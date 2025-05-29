package org.olzhas.project.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.olzhas.project.auth.CustomUserDetails;
import org.olzhas.project.dto.TaskFilter;
import org.olzhas.project.dto.TaskRequest;
import org.olzhas.project.dto.TaskResponse;
import org.olzhas.project.service.TaskService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    public Page<TaskResponse> list(
            @AuthenticationPrincipal CustomUserDetails principal,
            @ModelAttribute @Valid TaskFilter filter) {

        return taskService.findAll(principal.user().getId(), filter);
    }

    @GetMapping("/{id}")
    public TaskResponse getById(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long id) {

        return taskService.findById(principal.user().getId(), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse create(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody @Valid TaskRequest request) {

        return taskService.create(principal.user().getId(), request);
    }

    @PutMapping("/{id}")
    public TaskResponse update(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long id,
            @RequestBody @Valid TaskRequest request) {

        return taskService.update(principal.user().getId(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long id) {

        taskService.delete(principal.user().getId(), id);
    }
}
