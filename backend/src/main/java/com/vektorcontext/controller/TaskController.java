package com.vektorcontext.controller;

import com.vektorcontext.api.ApiResponse;
import com.vektorcontext.dto.TaskRequest;
import com.vektorcontext.dto.TaskResponse;
import com.vektorcontext.services.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TaskResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.ok("ok", taskService.listAll()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponse>> create(@RequestBody TaskRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("ok", taskService.create(req)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> update(@PathVariable Long id, @RequestBody TaskRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("ok", taskService.update(id, req)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        taskService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("ok"));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<TaskResponse>> complete(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("ok", taskService.complete(id)));
    }
}
