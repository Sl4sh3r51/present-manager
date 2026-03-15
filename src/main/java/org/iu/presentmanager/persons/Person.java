package org.iu.presentmanager.persons;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import org.iu.presentmanager.giftIdeas.GiftIdea;
import org.iu.presentmanager.gifts.Gift;
import org.iu.presentmanager.person_interests.PersonInterest;
import org.iu.presentmanager.tasks.Task;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
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
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private PersonStatus status;

    @Column(name = "birthday")
    private LocalDate birthday;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<PersonInterest> personInterests = new HashSet<>();

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<GiftIdea> giftIdeas = new HashSet<>();

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<Gift> gifts = new HashSet<>();

    @OneToMany(mappedBy = "person")
    @JsonIgnore
    private Set<Task> tasks = new HashSet<>();

}
