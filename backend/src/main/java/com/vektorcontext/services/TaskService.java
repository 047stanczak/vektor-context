package com.vektorcontext.services;

import com.vektorcontext.dto.TaskRequest;
import com.vektorcontext.dto.TaskResponse;
import com.vektorcontext.exception.DivergenceNotFoundException;
import com.vektorcontext.models.Task;
import com.vektorcontext.models.TaskCompletion;
import com.vektorcontext.repository.TaskCompletionRepository;
import com.vektorcontext.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskCompletionRepository completionRepository;

    public TaskService(TaskRepository taskRepository, TaskCompletionRepository completionRepository) {
        this.taskRepository = taskRepository;
        this.completionRepository = completionRepository;
    }

    public List<TaskResponse> listAll() {
        LocalDate[] week = currentWeekRange();
        return taskRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(t -> toResponse(t, week[0], week[1]))
                .toList();
    }

    public TaskResponse create(TaskRequest req) {
        Task task = new Task();
        task.setTitle(req.getTitle());
        task.setStatus("TODO");
        task.setPriority(req.getPriority() != null ? req.getPriority() : "MEDIUM");
        task.setEnergyLevel(req.getEnergyLevel() != null ? req.getEnergyLevel() : "QUICK");
        task.setFrequency(req.getFrequency() != null ? req.getFrequency() : "ONCE");
        task.setTags(req.getTags());
        task.setCreatedAt(LocalDateTime.now());
        taskRepository.save(task);
        LocalDate[] week = currentWeekRange();
        return toResponse(task, week[0], week[1]);
    }

    public TaskResponse update(Long id, TaskRequest req) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new DivergenceNotFoundException("Tarefa não encontrada: " + id));
        if (req.getTitle() != null) task.setTitle(req.getTitle());
        if (req.getStatus() != null) task.setStatus(req.getStatus());
        if (req.getPriority() != null) task.setPriority(req.getPriority());
        if (req.getEnergyLevel() != null) task.setEnergyLevel(req.getEnergyLevel());
        if (req.getFrequency() != null) task.setFrequency(req.getFrequency());
        if (req.getTags() != null) task.setTags(req.getTags());
        taskRepository.save(task);
        LocalDate[] week = currentWeekRange();
        return toResponse(task, week[0], week[1]);
    }

    @Transactional
    public void delete(Long id) {
        if (!taskRepository.existsById(id))
            throw new DivergenceNotFoundException("Tarefa não encontrada: " + id);
        completionRepository.deleteByTaskId(id);
        taskRepository.deleteById(id);
    }

    public TaskResponse complete(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new DivergenceNotFoundException("Tarefa não encontrada: " + id));

        LocalDate today = LocalDate.now();
        LocalDate[] week = currentWeekRange();

        if (task.getFrequency().equals("ONCE")) {
            task.setStatus("DONE");
            taskRepository.save(task);
        } else {
            TaskCompletion completion = new TaskCompletion();
            completion.setTask(task);
            completion.setCompletedOn(today);
            completionRepository.save(completion);
        }

        return toResponse(task, week[0], week[1]);
    }

    private TaskResponse toResponse(Task task, LocalDate weekStart, LocalDate weekEnd) {
        TaskResponse res = new TaskResponse();
        res.setId(task.getId());
        res.setTitle(task.getTitle());
        res.setStatus(task.getStatus());
        res.setPriority(task.getPriority());
        res.setEnergyLevel(task.getEnergyLevel());
        res.setFrequency(task.getFrequency());
        res.setTags(task.getTags());
        res.setCreatedAt(task.getCreatedAt());
        res.setWeeklyTarget(weeklyTarget(task.getFrequency()));
        res.setWeeklyDone(
            task.getFrequency().equals("ONCE") ? 0
                : completionRepository.countByTaskIdAndCompletedOnBetween(task.getId(), weekStart, weekEnd)
        );
        return res;
    }

    private int weeklyTarget(String frequency) {
        return switch (frequency) {
            case "WEEKLY_1X" -> 1;
            case "WEEKLY_2X" -> 2;
            case "WEEKLY_3X" -> 3;
            default -> 0;
        };
    }

    private LocalDate[] currentWeekRange() {
        LocalDate today = LocalDate.now();
        LocalDate start = today.with(DayOfWeek.MONDAY);
        LocalDate end = today.with(DayOfWeek.SUNDAY);
        return new LocalDate[]{start, end};
    }
}
