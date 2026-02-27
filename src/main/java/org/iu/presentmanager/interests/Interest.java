package org.iu.presentmanager.interests;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.iu.presentmanager.person_interests.PersonInterest;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "interests")
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Interest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "interest", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<PersonInterest> personInterests = new HashSet<>();
}
