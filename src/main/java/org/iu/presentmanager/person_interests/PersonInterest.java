package org.iu.presentmanager.person_interests;

import jakarta.persistence.*;
import lombok.*;
import org.iu.presentmanager.interests.Interest;
import org.iu.presentmanager.persons.Person;

@Entity
@Table(name = "person_interests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class PersonInterest {

    @EmbeddedId
    private PersonInterestId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("personId")
    @JoinColumn(name = "person_id")
    private Person person;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("interestId")
    @JoinColumn(name = "interest_id")
    private Interest interest;
}
