package org.olzhas.project.mapper;

import org.mapstruct.*;
import org.olzhas.project.dto.TaskResponse;
import org.olzhas.project.model.Task;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface TaskMapper {
    Task toEntity(TaskResponse taskResponse);

    TaskResponse toDto(Task task);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Task partialUpdate(TaskResponse taskResponse, @MappingTarget Task task);
}