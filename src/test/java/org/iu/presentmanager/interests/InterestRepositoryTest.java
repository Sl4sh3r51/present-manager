package org.iu.presentmanager.interests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

@DataJpaTest
@ActiveProfiles("test")
class InterestRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private InterestRepository interestRepository;

    private Interest interest1;
    private Interest interest2;
    private Interest interest3;

    @BeforeEach
    public void setUp() {
        // Initialize test data
        interest1 = new Interest();
        interest1.setName("Hiking");

        interest2 = new Interest();
        interest2.setName("Cooking");

        interest3 = new Interest();
        interest3.setName("Traveling");

        // Persist interests to the in-memory database
        entityManager.persistAndFlush(interest1);
        entityManager.persistAndFlush(interest2);
        entityManager.persistAndFlush(interest3);
    }

    @Test
    void shouldFindInterestByName(){
        Optional<Interest> result = interestRepository.findByNameIgnoreCase("Cooking");

        //Then
        assert(result.isPresent());
        assert(result.get().getName().equals("Cooking"));
    }

    @Test
    void shouldFindInterestByFullCapsName(){
        Optional<Interest> result = interestRepository.findByNameIgnoreCase("HIKING");

        //Then
        assert(result.isPresent());
        assert(result.get().getName().equals("Hiking"));
    }

    @Test
    void shouldFindInterestByLowerCaseName() {
        Optional<Interest> result = interestRepository.findByNameIgnoreCase("traveling");

        //Then
        assert (result.isPresent());
        assert (result.get().getName().equals("Traveling"));
    }

    @Test
    void shouldReturnEmptyWhenInterestNotFound() {
        Optional<Interest> result = interestRepository.findByNameIgnoreCase("Dancing");

        //Then
        assert (result.isEmpty());
    }

    @Test
    void shouldReturnTrueWhenInterestExists() {
        boolean exists = interestRepository.existsByNameIgnoreCase("Cooking");

        //Then
        assert (exists);
    }

    @Test
    void shouldReturnFalseWhenInterestDoesNotExist() {
        boolean exists = interestRepository.existsByNameIgnoreCase("Dancing");

        //Then
        assert (!exists);
    }
}
