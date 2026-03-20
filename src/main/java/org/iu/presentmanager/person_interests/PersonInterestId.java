package org.iu.presentmanager.person_interests;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonInterestId implements Serializable {

    @Column(name = "person_id")
    private UUID personId;

    @Column(name = "interest_id")
    private UUID interestId;

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (!(o instanceof PersonInterestId that)) { return false; }
        return Objects.equals(personId, that.personId) && Objects.equals(interestId, that.interestId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(personId, interestId);
    }
}
