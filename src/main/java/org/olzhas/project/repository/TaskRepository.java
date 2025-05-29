package org.olzhas.project.repository;

import org.olzhas.project.model.Task;
import org.olzhas.project.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long>,
        JpaSpecificationExecutor<Task> {
    List<Task> findAllByUserId(Long userId);
    Optional<Task> findByIdAndUserId(Long id, Long userId);
    List<Task> findAllByUserIdAndStatus(Long userId, TaskStatus status);
}