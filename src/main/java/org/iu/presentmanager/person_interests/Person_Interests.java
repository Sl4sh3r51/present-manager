package org.iu.presentmanager.person_interests;

import jakarta.persistence.*;
import lombok.*;
import org.iu.presentmanager.interests.Interests;
import org.iu.presentmanager.persons.Person;

@Entity
@Table(name = "person_interests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class Person_Interests {

    @EmbeddedId
    private PersonInterestsId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("personId")
    @JoinColumn(name = "person_id")
    private Person person;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("interestId")
    @JoinColumn(name = "interest_id")
    private Interests interests;
}
