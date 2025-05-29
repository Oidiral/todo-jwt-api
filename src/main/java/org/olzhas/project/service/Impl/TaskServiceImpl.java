package org.olzhas.project.service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.olzhas.project.ExceptionHandler.NotFoundException;
import org.olzhas.project.dto.TaskFilter;
import org.olzhas.project.dto.TaskRequest;
import org.olzhas.project.dto.TaskResponse;
import org.olzhas.project.mapper.TaskMapper;
import org.olzhas.project.model.Task;
import org.olzhas.project.model.User;
import org.olzhas.project.repository.TaskRepository;
import org.olzhas.project.service.TaskService;
import org.olzhas.project.spec.TaskSpecs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponse> findAll(Long userId, TaskFilter filter) {
        Specification<Task> spec = TaskSpecs.withFilter(filter, userId);

        Sort sort = Sort.by(Sort.Direction.fromString(filter.getDirection()),
                filter.getSort());

        Pageable pageable = PageRequest.of(
                filter.getPage(),
                filter.getSize(),
                sort);

        return taskRepository.findAll(spec, pageable).map(taskMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse findById(Long userId, Long taskId) {
        Task task = taskRepository.findByIdAndUserId(userId, taskId)
                .orElseThrow(() -> new NotFoundException("Task not found with id: " + taskId));

        return taskMapper.toDto(task);
    }

    @Override
    public TaskResponse create(Long userId, TaskRequest request) {
        Task task = new Task();
        task.setUser(User.builder().id(userId).build());
        task.setTitle(request.getTitle().trim());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());

        taskRepository.save(task);

        log.info("Created task '{}' for user ID {}", task.getTitle(), userId);
        return taskMapper.toDto(task);
    }


    @Override
    public TaskResponse update(Long userId, Long taskId, TaskRequest request) {
        Task task = taskRepository.findByIdAndUserId(userId, taskId)
                .orElseThrow(() -> new NotFoundException("Task not found with id: " + taskId));
        task.setTitle(request.getTitle().trim());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());
        taskRepository.save(task);
        log.info("Updated task '{}' for user ID {}", task.getTitle(), userId);
        return taskMapper.toDto(task);
    }

    @Override
    public void delete(Long userId, Long taskId) {
        Task task = taskRepository.findByIdAndUserId(userId, taskId)
                .orElseThrow(() -> new NotFoundException("Task not found with id: " + taskId));
        taskRepository.delete(task);
        log.info("Deleted task '{}' for user ID {}", task.getTitle(), userId);
    }
}
