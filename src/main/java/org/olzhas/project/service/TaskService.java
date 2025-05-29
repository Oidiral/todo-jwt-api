package org.olzhas.project.service;

import org.olzhas.project.dto.TaskFilter;
import org.olzhas.project.dto.TaskRequest;
import org.olzhas.project.dto.TaskResponse;
import org.springframework.data.domain.Page;


public interface TaskService {
    Page<TaskResponse> findAll(Long userId, TaskFilter filter);

    TaskResponse findById(Long userId, Long taskId);

    TaskResponse create(Long userId, TaskRequest request);

    TaskResponse update(Long userId, Long taskId, TaskRequest request);

    void delete(Long userId, Long taskId);
}
