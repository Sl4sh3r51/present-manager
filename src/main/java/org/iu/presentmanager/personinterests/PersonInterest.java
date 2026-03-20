package org.iu.presentmanager.personinterests;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.iu.presentmanager.interests.Interest;
import org.iu.presentmanager.persons.Person;

import java.util.Objects;

@Entity
@Table(name = "person_interests")
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

    public PersonInterestId getId() {
        return id == null ? null : new PersonInterestId(id.getPersonId(), id.getInterestId());
    }

    public void setId(PersonInterestId id) {
        this.id = id;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public Interest getInterest() {
        return interest;
    }

    public void setInterest(Interest interest) {
        this.interest = interest;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PersonInterest that)) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
