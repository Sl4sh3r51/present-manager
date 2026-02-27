package org.iu.presentmanager.person_interests;

import org.iu.presentmanager.config.WebConfig;
import org.iu.presentmanager.exceptions.DuplicateResourceException;
import org.iu.presentmanager.exceptions.ResourceNotFoundException;
import org.iu.presentmanager.interests.Interest;
import org.iu.presentmanager.persons.Person;
import org.iu.presentmanager.security.SecurityConfig;
import org.iu.presentmanager.security.SupabaseJwtConverter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PersonInterestController.class)
@ActiveProfiles("test")
@WithMockUser
@Import({SecurityConfig.class, WebConfig.class})
class PersonInterestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PersonInterestService personInterestService;

    @MockitoBean
    private SupabaseJwtConverter supabaseJwtConverter;

    private final UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private final UUID personId = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private final UUID interestId = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private final UUID interestId2 = UUID.fromString("00000000-0000-0000-0000-000000000004");

    private Person testPerson;
    private Interest testInterest;
    private PersonInterest testPersonInterest;

    @BeforeEach
    void setUp() {
        testPerson = createTestPerson("Alice", userId);
        testInterest = createTestInterest("Reading");
        testPersonInterest = createTestPersonInterest(personId, interestId, testPerson, testInterest);

        UsernamePasswordAuthenticationToken authenticationToken =
            new UsernamePasswordAuthenticationToken(userId, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private Person createTestPerson(String name, UUID userId) {
        Person person = new Person();
        person.setId(personId);
        person.setName(name);
        person.setUserId(userId);
        person.setBirthday(LocalDate.of(1990, 3, 15));
        return person;
    }

    private Interest createTestInterest(String name) {
        Interest interest = new Interest();
        interest.setId(UUID.randomUUID());
        interest.setName(name);
        return interest;
    }

    private PersonInterest createTestPersonInterest(UUID pId, UUID iId, Person person, Interest interest) {
        PersonInterest pi = new PersonInterest();
        pi.setId(new PersonInterestId(pId, iId));
        pi.setPerson(person);
        pi.setInterest(interest);
        return pi;
    }

    @Test
    void shouldAddInterestSuccessfully() throws Exception {
        // GIVEN
        when(personInterestService.addInterest(personId, userId, interestId))
                .thenReturn(testPersonInterest);

        // WHEN & THEN
        mockMvc.perform(post("/person-interests")
                .param("personId", personId.toString())
                .param("interestId", interestId.toString())
                .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id.personId").value(personId.toString()))
                .andExpect(jsonPath("$.id.interestId").value(interestId.toString()));

        verify(personInterestService).addInterest(personId, userId, interestId);
    }

    @Test
    void shouldReturnNotFoundWhenPersonNotFound() throws Exception {
        // GIVEN
        when(personInterestService.addInterest(personId, userId, interestId))
                .thenThrow(new ResourceNotFoundException("Person not found"));

        // WHEN & THEN
        mockMvc.perform(post("/person-interests")
                .param("personId", personId.toString())
                .param("interestId", interestId.toString())
                .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound());

        verify(personInterestService).addInterest(personId, userId, interestId);
    }

    @Test
    void shouldReturnConflictWhenDuplicate() throws Exception {
        // GIVEN
        when(personInterestService.addInterest(personId, userId, interestId))
                .thenThrow(new DuplicateResourceException("Person already has interest"));

        // WHEN & THEN
        mockMvc.perform(post("/person-interests")
                .param("personId", personId.toString())
                .param("interestId", interestId.toString())
                .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isConflict());

        verify(personInterestService).addInterest(personId, userId, interestId);
    }

    @Test
    void shouldAddMultipleInterestsSuccessfully() throws Exception {
        // GIVEN
        Set<UUID> interestIds = Set.of(interestId, interestId2);
        when(personInterestService.addMultipleInterests(personId, userId, interestIds))
                .thenReturn(List.of(testPersonInterest));

        // WHEN & THEN
        mockMvc.perform(post("/person-interests/batch")
                .param("personId", personId.toString())
                .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(interestIds)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(personInterestService).addMultipleInterests(personId, userId, interestIds);
    }

    @Test
    void shouldGetPersonInterests() throws Exception {
        // GIVEN
        when(personInterestService.getPersonInterests(personId, userId))
                .thenReturn(List.of(testPersonInterest));

        // WHEN & THEN
        mockMvc.perform(get("/person-interests")
                .param("personId", personId.toString())
                .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id.personId").value(personId.toString()));

        verify(personInterestService).getPersonInterests(personId, userId);
    }

    @Test
    void shouldReturnEmptyListWhenPersonHasNoInterests() throws Exception {
        // GIVEN
        when(personInterestService.getPersonInterests(personId, userId))
                .thenReturn(List.of());

        // WHEN & THEN
        mockMvc.perform(get("/person-interests")
                .param("personId", personId.toString())
                .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(personInterestService).getPersonInterests(personId, userId);
    }

    @Test
    void shouldGetInterestsForPerson() throws Exception {
        // GIVEN
        when(personInterestService.getInterestsForPerson(personId, userId))
                .thenReturn(List.of(testInterest));

        // WHEN & THEN
        mockMvc.perform(get("/person-interests/interests")
                .param("personId", personId.toString())
                .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Reading"));

        verify(personInterestService).getInterestsForPerson(personId, userId);
    }

    @Test
    void shouldGetPersonsForInterest() throws Exception {
        // GIVEN
        when(personInterestService.getPersonsForInterest(interestId, userId))
                .thenReturn(List.of(testPerson));

        // WHEN & THEN
        mockMvc.perform(get("/person-interests/persons")
                .param("interestId", interestId.toString())
                .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Alice"));

        verify(personInterestService).getPersonsForInterest(interestId, userId);
    }

    @Test
    void shouldGetAllByUser() throws Exception {
        // GIVEN
        when(personInterestService.getAllByUser(userId))
                .thenReturn(List.of(testPersonInterest));

        // WHEN & THEN
        mockMvc.perform(get("/person-interests/all")
                .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(personInterestService).getAllByUser(userId);
    }

    @Test
    void shouldCheckIfPersonHasInterest() throws Exception {
        // GIVEN
        when(personInterestService.hasInterest(personId, userId, interestId))
                .thenReturn(true);

        // WHEN & THEN
        mockMvc.perform(get("/person-interests/check")
                .param("personId", personId.toString())
                .param("interestId", interestId.toString())
                .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(personInterestService).hasInterest(personId, userId, interestId);
    }

    @Test
    void shouldReturnFalseWhenPersonDoesntHaveInterest() throws Exception {
        // GIVEN
        when(personInterestService.hasInterest(personId, userId, interestId))
                .thenReturn(false);

        // WHEN & THEN
        mockMvc.perform(get("/person-interests/check")
                .param("personId", personId.toString())
                .param("interestId", interestId.toString())
                .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(personInterestService).hasInterest(personId, userId, interestId);
    }

    @Test
    void shouldReplaceAllInterests() throws Exception {
        // GIVEN
        Set<UUID> newInterestIds = Set.of(interestId2);
        doNothing().when(personInterestService).replaceAllInterests(personId, userId, newInterestIds);

        // WHEN & THEN
        mockMvc.perform(put("/person-interests/replace")
                .param("personId", personId.toString())
                .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newInterestIds)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        verify(personInterestService).replaceAllInterests(personId, userId, newInterestIds);
    }

    @Test
    void shouldReturnNotFoundWhenReplacingForNonExistentPerson() throws Exception {
        // GIVEN
        Set<UUID> newInterestIds = Set.of(interestId2);
        doThrow(new ResourceNotFoundException("Person not found"))
                .when(personInterestService).replaceAllInterests(personId, userId, newInterestIds);

        // WHEN & THEN
        mockMvc.perform(put("/person-interests/replace")
                .param("personId", personId.toString())
                .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newInterestIds)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound());

        verify(personInterestService).replaceAllInterests(personId, userId, newInterestIds);
    }

    @Test
    void shouldRemoveInterest() throws Exception {
        // GIVEN
        doNothing().when(personInterestService).removeInterest(personId, userId, interestId);

        // WHEN & THEN
        mockMvc.perform(delete("/person-interests")
                .param("personId", personId.toString())
                .param("interestId", interestId.toString())
                .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNoContent());

        verify(personInterestService).removeInterest(personId, userId, interestId);
    }

    @Test
    void shouldReturnNotFoundWhenRemovingNonExistentInterest() throws Exception {
        // GIVEN
        doThrow(new ResourceNotFoundException("Interest not found"))
                .when(personInterestService).removeInterest(personId, userId, interestId);

        // WHEN & THEN
        mockMvc.perform(delete("/person-interests")
                .param("personId", personId.toString())
                .param("interestId", interestId.toString())
                .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound());

        verify(personInterestService).removeInterest(personId, userId, interestId);
    }

    @Test
    void shouldRemoveAllInterests() throws Exception {
        // GIVEN
        doNothing().when(personInterestService).removeAllInterests(personId, userId);

        // WHEN & THEN
        mockMvc.perform(delete("/person-interests/all")
                .param("personId", personId.toString())
                .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNoContent());

        verify(personInterestService).removeAllInterests(personId, userId);
    }

    @Test
    void shouldCountInterestsForPerson() throws Exception {
        // GIVEN
        when(personInterestService.countInterestsForPerson(personId, userId))
                .thenReturn(3L);

        // WHEN & THEN
        mockMvc.perform(get("/person-interests/count/person")
                .param("personId", personId.toString())
                .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(content().string("3"));

        verify(personInterestService).countInterestsForPerson(personId, userId);
    }

    @Test
    void shouldCountPersonsWithInterest() throws Exception {
        // GIVEN
        when(personInterestService.countPersonsWithInterest(interestId, userId))
                .thenReturn(5L);

        // WHEN & THEN
        mockMvc.perform(get("/person-interests/count/interest")
                .param("interestId", interestId.toString())
                .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(content().string("5"));

        verify(personInterestService).countPersonsWithInterest(interestId, userId);
    }
}
