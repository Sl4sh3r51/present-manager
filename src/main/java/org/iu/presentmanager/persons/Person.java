package org.iu.presentmanager.persons;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "persons",
        indexes = {
                @Index(name = "idx_persons_user_id", columnList = "user_id"),
                @Index(name = "idx_persons_status", columnList = "status"),
                @Index(name = "idx_persons_birthday", columnList = "birthday")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Person {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    @JsonIgnore
    private UUID userId;

    @NotBlank(message = "Name is mandatory")
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull(message = "Status is mandatory")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PersonStatus status;

    @Column(name = "birthday")
    private LocalDate birthday;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
