package org.iu.presentmanager.tasks;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.iu.presentmanager.persons.Person;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "tasks",
        indexes = {
                @Index(name = "idx_tasks_person_id", columnList = "person_id"),
                @Index(name = "idx_tasks_user_id", columnList = "user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull(message = "Person ID is required")
    @Column(name = "person_id", nullable = false)
    private UUID personId;

    @Column(name = "user_id", nullable = false)
    @JsonIgnore
    private UUID userId;

    @NotBlank(message = "Title is required")
    @Column(name = "title", nullable = false)
    private String title;

    @NotNull(message = "Is done is required")
    @Column(name = "is_done", nullable = false)
    private Boolean isDone = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", insertable = false, updatable = false)
    @JsonIgnore
    private Person person;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Task that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
