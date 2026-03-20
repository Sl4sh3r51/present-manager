package org.iu.presentmanager.occasions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(
        name = "occasions",
        indexes = {
                @Index(name = "idx_occasions_user_id", columnList = "user_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "unique_user_occasion", columnNames = {"user_id", "name"})
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Occasion {

    private static final int MAX_MONTH = 12;
    private static final int MAX_DAY = 31;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    @JsonIgnore
    private UUID userId;

    @NotBlank(message = "Name is mandatory")
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull(message = "Type is mandatory")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "type", nullable = false)
    private OccasionType type;

    @Min(value = 1, message = "Month must be between 1 and 12")
    @Max(value = MAX_MONTH, message = "Month must be between 1 and 12")
    @Column(name = "fixed_month")
    private Integer fixedMonth;

    @Min(value = 1, message = "Day must be between 1 and 31")
    @Max(value = MAX_DAY, message = "Day must be between 1 and 31")
    @Column(name = "fixed_day")
    private Integer fixedDay;

    @NotNull(message = "Is recurring is required")
    @Column(name = "is_recurring", nullable = false)
    private Boolean isRecurring = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "occasion")
    @JsonIgnore
    private Set<GiftIdea> giftIdeas = new HashSet<>();

    @OneToMany(mappedBy = "occasion")
    @JsonIgnore
    private Set<Gift> gifts = new HashSet<>();

}
