package org.olzhas.project.spec;

import jakarta.persistence.criteria.Predicate;
import lombok.experimental.UtilityClass;
import org.olzhas.project.dto.TaskFilter;
import org.olzhas.project.model.Task;
import org.springframework.data.jpa.domain.Specification;

@UtilityClass
public class TaskSpecs {

    public Specification<Task> withFilter(TaskFilter f, Long userId){
        return (root, query, cb) -> {
            Predicate p = cb.equal(root.get("user").get("id"), userId);
            if (f.getTitle() != null && !f.getTitle().isBlank()) {
                p = cb.and(p, cb.like(cb.lower(root.get("title")),
                        "%" + f.getTitle().toLowerCase() + "%"));
            }
            if (f.getDescription() != null && !f.getDescription().isBlank()) {
                p = cb.and(p, cb.like(cb.lower(root.get("description")),
                        "%" + f.getDescription().toLowerCase() + "%"));
            }
            if (f.getStatus() != null) {
                p = cb.and(p, cb.equal(root.get("status"), f.getStatus()));
            }
            return p;
        };
    }
}
