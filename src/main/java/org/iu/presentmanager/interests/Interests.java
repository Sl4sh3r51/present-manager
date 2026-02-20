package org.iu.presentmanager.interests;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.iu.presentmanager.person_interests.Person_Interests;

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
public class Interests {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "interest", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<Person_Interests> personInterests = new HashSet<>();
}
