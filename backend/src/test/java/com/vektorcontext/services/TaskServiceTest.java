package com.vektorcontext.services;

import com.vektorcontext.dto.TaskRequest;
import com.vektorcontext.dto.TaskResponse;
import com.vektorcontext.exception.DivergenceNotFoundException;
import com.vektorcontext.models.Task;
import com.vektorcontext.repository.TaskCompletionRepository;
import com.vektorcontext.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskCompletionRepository completionRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    void listAll_returnsTasksOrderedByCreatedAtDesc() {
        Task task = new Task();
        task.setTitle("Tarefa 1");
        task.setFrequency("WEEKLY_2X");
        when(taskRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(task));
        when(completionRepository.countByTaskIdAndCompletedOnBetween(any(), any(), any())).thenReturn(1);

        List<TaskResponse> result = taskService.listAll();

        assertEquals(1, result.size());
        assertEquals("Tarefa 1", result.get(0).getTitle());
        assertEquals(2, result.get(0).getWeeklyTarget());
        assertEquals(1, result.get(0).getWeeklyDone());
    }

    @Test
    void listAll_returnsEmptyListWhenNoTasks() {
        when(taskRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of());

        List<TaskResponse> result = taskService.listAll();

        assertTrue(result.isEmpty());
    }

    @Test
    void create_usesDefaultsWhenFieldsAreNull() {
        TaskRequest req = new TaskRequest();
        req.setTitle("Nova tarefa");

        TaskResponse result = taskService.create(req);

        assertEquals("Nova tarefa", result.getTitle());
        assertEquals("TODO", result.getStatus());
        assertEquals("MEDIUM", result.getPriority());
        assertEquals("QUICK", result.getEnergyLevel());
        assertEquals("ONCE", result.getFrequency());
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void create_usesProvidedValues() {
        TaskRequest req = new TaskRequest();
        req.setTitle("Nova tarefa");
        req.setPriority("HIGH");
        req.setEnergyLevel("FOCUS");
        req.setFrequency("WEEKLY_1X");

        TaskResponse result = taskService.create(req);

        assertEquals("HIGH", result.getPriority());
        assertEquals("FOCUS", result.getEnergyLevel());
        assertEquals("WEEKLY_1X", result.getFrequency());
        assertEquals(1, result.getWeeklyTarget());
    }

    @Test
    void update_updatesOnlyProvidedFields() {
        Task task = new Task();
        task.setTitle("Antigo");
        task.setStatus("TODO");
        task.setPriority("LOW");
        task.setEnergyLevel("QUICK");
        task.setFrequency("ONCE");
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        TaskRequest req = new TaskRequest();
        req.setTitle("Atualizado");

        TaskResponse result = taskService.update(1L, req);

        assertEquals("Atualizado", result.getTitle());
        assertEquals("LOW", result.getPriority());
        verify(taskRepository).save(task);
    }

    @Test
    void update_notFound_throwsException() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        TaskRequest req = new TaskRequest();
        assertThrows(DivergenceNotFoundException.class, () -> taskService.update(1L, req));
    }

    @Test
    void delete_removesTaskAndCompletions() {
        when(taskRepository.existsById(1L)).thenReturn(true);

        taskService.delete(1L);

        verify(completionRepository).deleteByTaskId(1L);
        verify(taskRepository).deleteById(1L);
    }

    @Test
    void delete_notFound_throwsException() {
        when(taskRepository.existsById(1L)).thenReturn(false);

        assertThrows(DivergenceNotFoundException.class, () -> taskService.delete(1L));
        verify(taskRepository, never()).deleteById(any());
    }

    @Test
    void complete_onceTask_setsStatusDone() {
        Task task = new Task();
        task.setFrequency("ONCE");
        task.setStatus("TODO");
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        TaskResponse result = taskService.complete(1L);

        assertEquals("DONE", task.getStatus());
        verify(taskRepository).save(task);
        verify(completionRepository, never()).save(any());
        assertEquals(0, result.getWeeklyDone());
    }

    @Test
    void complete_recurringTask_registersCompletion() {
        Task task = new Task();
        task.setFrequency("WEEKLY_2X");
        task.setStatus("TODO");
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(completionRepository.countByTaskIdAndCompletedOnBetween(any(), any(), any())).thenReturn(1);

        taskService.complete(1L);

        verify(completionRepository).save(any());
        verify(taskRepository, never()).save(any());
    }

    @Test
    void complete_notFound_throwsException() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(DivergenceNotFoundException.class, () -> taskService.complete(1L));
    }
}
