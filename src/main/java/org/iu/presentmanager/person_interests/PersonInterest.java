package org.iu.presentmanager.person_interests;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.iu.presentmanager.interests.Interest;
import org.iu.presentmanager.persons.Person;

@Entity
@Table(name = "person_interests")
@Data
@NoArgsConstructor
@AllArgsConstructor
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
