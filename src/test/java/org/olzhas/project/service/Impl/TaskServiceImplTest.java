package org.olzhas.project.service.Impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.olzhas.project.exceptionHandler.NotFoundException;
import org.olzhas.project.dto.TaskFilter;
import org.olzhas.project.dto.TaskRequest;
import org.olzhas.project.dto.TaskResponse;
import org.olzhas.project.mapper.TaskMapper;
import org.olzhas.project.model.Task;
import org.olzhas.project.model.User;
import org.olzhas.project.repository.TaskRepository;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskServiceImpl taskService;

    @Test
    void findAll_shouldReturnMappedPage() {
        Long userId = 1L;
        TaskFilter filter = new TaskFilter();
        filter.setPage(0);
        filter.setSize(10);
        filter.setSort("id");
        filter.setDirection("ASC");

        Task task = new Task();
        TaskResponse dto = new TaskResponse();
        when(taskMapper.toDto(task)).thenReturn(dto);

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));
        Page<Task> taskPage = new PageImpl<>(List.of(task), pageable, 1);
        when(taskRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(taskPage);

        Page<TaskResponse> result = taskService.findAll(userId, filter);

        assertEquals(1, result.getTotalElements(), "Ожидаем ровно одну запись");
        assertSame(dto, result.getContent().get(0), "Первый элемент должен быть тем же DTO");
        verify(taskRepository).findAll(any(Specification.class), eq(pageable));
        verify(taskMapper).toDto(task);
    }

    @Test
    void findById_shouldReturnMappedResponse() {
        Long userId = 1L;
        Long taskId = 42L;
        Task task = new Task();
        TaskResponse dto = new TaskResponse();

        when(taskRepository.findByIdAndUserId(userId, taskId))
                .thenReturn(Optional.of(task));
        when(taskMapper.toDto(task)).thenReturn(dto);

        TaskResponse result = taskService.findById(userId, taskId);

        assertSame(dto, result, "Возвращаемое значение должно совпадать с замоканным DTO");
        verify(taskRepository).findByIdAndUserId(userId, taskId);
        verify(taskMapper).toDto(task);
    }

    @Test
    void findById_shouldThrowNotFoundException() {
        Long userId = 1L;
        Long taskId = 99L;
        when(taskRepository.findByIdAndUserId(userId, taskId))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> taskService.findById(userId, taskId),
                "При отсутствии задачи ожидаем NotFoundException");
        verify(taskRepository).findByIdAndUserId(userId, taskId);
        verifyNoInteractions(taskMapper);
    }

    @Test
    void create_shouldSaveAndReturnDto() {
        Long userId = 123L;
        TaskRequest request = new TaskRequest();
        request.setTitle("  My Task  ");
        request.setDescription("Описание задачи");
        request.setStatus(null); // null для упрощения

        TaskResponse expectedDto = new TaskResponse();
        when(taskMapper.toDto(any(Task.class))).thenReturn(expectedDto);

        TaskResponse actual = taskService.create(userId, request);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(captor.capture());
        Task saved = captor.getValue();

        assertEquals(userId, saved.getUser().getId());
        assertEquals("My Task", saved.getTitle());
        assertEquals("Описание задачи", saved.getDescription());
        assertNull(saved.getStatus());

        verify(taskMapper).toDto(saved);
        assertSame(expectedDto, actual);
    }

    @Test
    @DisplayName("update: должен обновить существующую задачу и вернуть DTO")
    void update_shouldSaveAndReturnDto() {
        Long userId = 12L, taskId = 34L;
        TaskRequest request = new TaskRequest();
        request.setTitle("  New Title ");
        request.setDescription("Новый дедскрипшн");
        request.setStatus(null);

        Task existing = new Task();
        existing.setUser(User.builder().id(userId).build());
        existing.setTitle("old");
        when(taskRepository.findByIdAndUserId(userId, taskId))
                .thenReturn(Optional.of(existing));

        TaskResponse expectedDto = new TaskResponse();
        when(taskMapper.toDto(existing)).thenReturn(expectedDto);

        TaskResponse actual = taskService.update(userId, taskId, request);

        verify(taskRepository).findByIdAndUserId(userId, taskId);
        verify(taskRepository).save(existing);

        assertEquals("New Title", existing.getTitle());
        assertEquals("Новый дедскрипшн", existing.getDescription());
        assertNull(existing.getStatus());

        verify(taskMapper).toDto(existing);
        assertSame(expectedDto, actual);
    }

    @Test
    @DisplayName("update: должен бросить NotFoundException, если задачи нет")
    void update_shouldThrowWhenNotFound() {
        Long userId = 1L, taskId = 2L;
        when(taskRepository.findByIdAndUserId(userId, taskId))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> taskService.update(userId, taskId, new TaskRequest()));
        verify(taskRepository).findByIdAndUserId(userId, taskId);
        verify(taskRepository, never()).save(any());
        verifyNoInteractions(taskMapper);
    }

    @Test
    @DisplayName("delete: должен удалить задачу, если она существует")
    void delete_shouldRemoveTask() {
        Long userId = 5L, taskId = 6L;
        Task toDelete = new Task();
        toDelete.setUser(User.builder().id(userId).build());
        toDelete.setTitle("to remove");
        when(taskRepository.findByIdAndUserId(userId, taskId))
                .thenReturn(Optional.of(toDelete));

        taskService.delete(userId, taskId);

        verify(taskRepository).findByIdAndUserId(userId, taskId);
        verify(taskRepository).delete(toDelete);
    }

    @Test
    @DisplayName("delete: должен бросить NotFoundException, если задачи нет")
    void delete_shouldThrowWhenNotFound() {
        Long userId = 7L, taskId = 8L;
        when(taskRepository.findByIdAndUserId(userId, taskId))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> taskService.delete(userId, taskId));
        verify(taskRepository).findByIdAndUserId(userId, taskId);
        verify(taskRepository, never()).delete((Task) any());
    }
}