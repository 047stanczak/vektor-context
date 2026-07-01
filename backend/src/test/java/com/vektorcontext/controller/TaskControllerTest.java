package com.vektorcontext.controller;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.vektorcontext.api.ApiResponse;
import com.vektorcontext.dto.TaskRequest;
import com.vektorcontext.dto.TaskResponse;
import com.vektorcontext.services.TaskService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskController controller;

    @Test
    void list_success_returnsTaskList() {
        TaskResponse t1 = new TaskResponse();
        t1.setId(1L);
        TaskResponse t2 = new TaskResponse();
        t2.setId(2L);
        when(taskService.listAll()).thenReturn(List.of(t1, t2));

        ResponseEntity<ApiResponse<List<TaskResponse>>> response = controller.list();

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        ApiResponse<List<TaskResponse>> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.success()).isTrue();
        assertThat(body.data()).hasSize(2);
    }

    @Test
    void list_empty_returnsEmptyList() {
        when(taskService.listAll()).thenReturn(List.of());

        ResponseEntity<ApiResponse<List<TaskResponse>>> response = controller.list();

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody().data()).isEmpty();
    }

    @Test
    void list_exception_propagates() {
        when(taskService.listAll()).thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> controller.list())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB error");
    }

    @Test
    void create_success_returnsTask() {
        TaskRequest req = new TaskRequest();
        req.setTitle("New Task");
        TaskResponse resp = new TaskResponse();
        resp.setId(10L);
        resp.setTitle("New Task");
        when(taskService.create(req)).thenReturn(resp);

        ResponseEntity<ApiResponse<TaskResponse>> response = controller.create(req);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        ApiResponse<TaskResponse> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.data().getId()).isEqualTo(10L);
        assertThat(body.data().getTitle()).isEqualTo("New Task");
    }

    @Test
    void create_exception_propagates() {
        TaskRequest req = new TaskRequest();
        when(taskService.create(req)).thenThrow(new RuntimeException("Create failed"));

        assertThatThrownBy(() -> controller.create(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Create failed");
    }

    @Test
    void update_success_returnsUpdatedTask() {
        Long id = 1L;
        TaskRequest req = new TaskRequest();
        req.setTitle("Updated");
        TaskResponse resp = new TaskResponse();
        resp.setId(id);
        resp.setTitle("Updated");
        when(taskService.update(id, req)).thenReturn(resp);

        ResponseEntity<ApiResponse<TaskResponse>> response = controller.update(id, req);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody().data().getTitle()).isEqualTo("Updated");
    }

    @Test
    void update_notFound_propagatesException() {
        Long id = 99L;
        TaskRequest req = new TaskRequest();
        when(taskService.update(id, req)).thenThrow(new RuntimeException("Not found"));

        assertThatThrownBy(() -> controller.update(id, req))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Not found");
    }

    @Test
    void delete_success_returnsOk() {
        Long id = 1L;

        ResponseEntity<ApiResponse<Void>> response = controller.delete(id);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody().success()).isTrue();
        verify(taskService).delete(id);
    }

    @Test
    void delete_notFound_propagatesException() {
        Long id = 99L;
        doThrow(new RuntimeException("Not found")).when(taskService).delete(id);

        assertThatThrownBy(() -> controller.delete(id))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Not found");
    }

    @Test
    void complete_success_returnsCompletedTask() {
        Long id = 1L;
        TaskResponse resp = new TaskResponse();
        resp.setId(id);
        resp.setStatus("DONE");
        when(taskService.complete(id)).thenReturn(resp);

        ResponseEntity<ApiResponse<TaskResponse>> response = controller.complete(id);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody().data().getStatus()).isEqualTo("DONE");
    }

    @Test
    void complete_notFound_propagatesException() {
        Long id = 99L;
        when(taskService.complete(id)).thenThrow(new RuntimeException("Not found"));

        assertThatThrownBy(() -> controller.complete(id))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Not found");
    }
}