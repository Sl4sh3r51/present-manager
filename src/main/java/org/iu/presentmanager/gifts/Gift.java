package org.iu.presentmanager.gifts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.iu.presentmanager.giftIdeas.GiftIdea;
import org.iu.presentmanager.occasions.Occasion;
import org.iu.presentmanager.persons.Person;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "gifts", indexes = {
        @Index(name = "idx_gifts_person_id", columnList = "person_id"),
        @Index(name = "idx_gifts_user_id", columnList = "user_id"),
        @Index(name = "idx_gifts_occasion_id", columnList = "occasion_id"),
        @Index(name = "idx_gifts_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Gift {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull(message = "Person ID is required")
    @Column(name = "person_id", nullable = false)
    private UUID personId;

    @Column(name = "user_id", nullable = false)
    @JsonIgnore
    private UUID userId;

    @Column(name = "occasion_id", nullable = false)
    private UUID occasionId;

    @Column(name = "gift_idea_id")
    private UUID giftIdeaId;

    @NotBlank(message = "Title is required")
    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "link")
    private String link;

    @Column(name = "image_url")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private GiftStatus status = GiftStatus.PLANNED;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "given_date")
    private LocalDate givenDate;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "occasion_id", insertable = false, updatable = false)
    @JsonIgnore
    private Occasion occasion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gift_idea_id", insertable = false, updatable = false)
    @JsonIgnore
    private GiftIdea giftIdea;

}
