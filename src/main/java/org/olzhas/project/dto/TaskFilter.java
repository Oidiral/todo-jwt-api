package org.olzhas.project.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.olzhas.project.model.TaskStatus;

@Data
@NoArgsConstructor
public class TaskFilter {
    private String title;
    private String description;
    private TaskStatus status;
    private String sort = "createdAt";
    private String direction = "DESC";
    private Integer page = 0;
    private Integer size = 10;

}
