package org.iu.presentmanager.persons;

import org.iu.presentmanager.config.WebConfig;
import org.iu.presentmanager.exceptions.ResourceNotFoundException;
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
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PersonController.class)
@ActiveProfiles("test")
@WithMockUser
@Import({SecurityConfig.class, WebConfig.class})
class   PersonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PersonService personService;

    @MockitoBean
    private SupabaseJwtConverter supabaseJwtConverter;

    private final UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private final UUID personId = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private Person testPerson;

    @BeforeEach
    void setUp() {
        testPerson = createTestPerson("Alice", PersonStatus.PLANNED, LocalDate.of(1990, 3, 15));

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userId, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private Person createTestPerson(String name, PersonStatus status, LocalDate birthday) {
        Person person = new Person();
        person.setId(personId);
        person.setName(name);
        person.setUserId(userId);
        person.setStatus(status);
        person.setBirthday(birthday);
        return person;
    }

    @Test
    void shouldReturnAllPersons() throws Exception {
        // GIVEN
        when(personService.getAllPersonsByUser(userId))
                .thenReturn(List.of(testPerson));

        // WHEN & THEN
        mockMvc.perform(get("/persons").with(jwt().jwt(builder -> builder.subject(userId.toString())))
                .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(personId.toString()))
                .andExpect(jsonPath("$[0].name").value("Alice"))
                .andExpect(jsonPath("$[0].status").value("PLANNED"));

        verify(personService).getAllPersonsByUser(userId);
        verifyNoMoreInteractions(personService);
    }

    @Test
    void shouldReturnEmptyListWhenNoPersons() throws Exception {
        // GIVEN
        when(personService.getAllPersonsByUser(userId))
                .thenReturn(Collections.emptyList());

        // WHEN & THEN
        mockMvc.perform(get("/persons")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(personService).getAllPersonsByUser(userId);
    }

    @Test
    void shouldFilterPersonsByStatus() throws Exception {
        // GIVEN
        when(personService.getPersonsByStatus(userId, PersonStatus.PLANNED))
                .thenReturn(List.of(testPerson));

        // WHEN & THEN
        mockMvc.perform(get("/persons")
                .param("status", "PLANNED")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(personService).getPersonsByStatus(userId, PersonStatus.PLANNED);
        verify(personService, never()).getAllPersonsByUser(any());
    }

    @Test
    void shouldReturnPersonById() throws Exception {
        // GIVEN
        when(personService.getPersonById(personId, userId))
                .thenReturn(testPerson);

        // WHEN & THEN
        mockMvc.perform(get("/persons/{id}", personId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(personId.toString()))
                .andExpect(jsonPath("$.name").value("Alice"));

        verify(personService).getPersonById(personId, userId);
    }

    @Test
    void shouldReturnNotFoundWhenPersonDoesNotExist() throws Exception {
        // GIVEN
        when(personService.getPersonById(personId, userId))
                .thenThrow(new ResourceNotFoundException("Person not found"));

        // WHEN & THEN
        mockMvc.perform(get("/persons/{id}", personId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound());

        verify(personService).getPersonById(personId, userId);
    }

    @Test
    void shouldCreatePerson() throws Exception {
        // GIVEN
        Person newPerson = new Person();
        newPerson.setName("Bob");
        newPerson.setStatus(PersonStatus.IDEAS);
        newPerson.setUserId(userId);
        newPerson.setBirthday(LocalDate.of(1985, 6, 20));

        testPerson.setId(UUID.randomUUID());
        when(personService.createPerson(any(Person.class), any(UUID.class)))
                .thenReturn(testPerson);

        // WHEN & THEN
        mockMvc.perform(post("/persons")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newPerson)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Alice"));

        verify(personService).createPerson(any(Person.class), any(UUID.class));
    }

    @Test
    void shouldReturnBadRequestForInvalidPerson() throws Exception {
        // GIVEN
        String invalidPersonJson = "{\"status\":\"PLANNED\"}"; // Name fehlt (mandatory)

        // WHEN & THEN
        mockMvc.perform(post("/persons")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                .contentType(APPLICATION_JSON)
                .content(invalidPersonJson))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        verify(personService, never()).createPerson(any(), any());
    }

    @Test
    void shouldReturnBadRequestForNullStatus() throws Exception {
        // GIVEN
        Person invalidPerson = new Person();
        invalidPerson.setName("Charlie");
        invalidPerson.setUserId(userId);
        invalidPerson.setBirthday(LocalDate.of(1995, 8, 10));
        String invalidPersonJson = objectMapper.writeValueAsString(invalidPerson);

        // WHEN & THEN
        mockMvc.perform(post("/persons")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                .contentType(APPLICATION_JSON)
                .content(invalidPersonJson))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        verify(personService, never()).createPerson(any(), any());
    }

    @Test
    void shouldDeletePerson() throws Exception {
        // GIVEN
        doNothing().when(personService).deletePerson(personId, userId);

        // WHEN & THEN
        mockMvc.perform(delete("/persons/{id}", personId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNoContent());

        verify(personService).deletePerson(personId, userId);
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentPerson() throws Exception {
        // GIVEN
        doThrow(new ResourceNotFoundException("Person not found"))
                .when(personService).deletePerson(personId, userId);

        // WHEN & THEN
        mockMvc.perform(delete("/persons/{id}", personId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound());

        verify(personService).deletePerson(personId, userId);
    }

    @Test
    void shouldReturnTodaysBirthdays() throws Exception {
        // GIVEN
        when(personService.getTodaysBirthdays(userId))
                .thenReturn(List.of(testPerson));

        // WHEN & THEN
        mockMvc.perform(get("/persons/birthdays/today")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(personService).getTodaysBirthdays(userId);
    }

    @Test
    void shouldReturnEmptyListWhenNoTodaysBirthdays() throws Exception {
        // GIVEN
        when(personService.getTodaysBirthdays(userId))
                .thenReturn(Collections.emptyList());

        // WHEN & THEN
        mockMvc.perform(get("/persons/birthdays/today")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }


    @Test
    void shouldReturnBirthdaysInMonth() throws Exception {
        // GIVEN
        int march = 3;
        when(personService.getBirthdaysInMonth(userId, march))
                .thenReturn(List.of(testPerson));

        // WHEN & THEN
        mockMvc.perform(get("/persons/birthdays/month/{month}", march)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(personService).getBirthdaysInMonth(userId, march);
    }

    @Test
    void shouldReturnEmptyListForMonthWithoutBirthdays() throws Exception {
        // GIVEN
        int december = 12;
        when(personService.getBirthdaysInMonth(userId, december))
                .thenReturn(Collections.emptyList());

        // WHEN & THEN
        mockMvc.perform(get("/persons/birthdays/month/{month}", december)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldReturnCountByStatus() throws Exception {
        // GIVEN
        when(personService.countByStatus(userId, PersonStatus.PLANNED))
                .thenReturn(5L);

        // WHEN & THEN
        mockMvc.perform(get("/persons/stats/count")
                .param("status", "PLANNED")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(content().string("5"));

        verify(personService).countByStatus(userId, PersonStatus.PLANNED);
    }

    @Test
    void shouldReturnZeroCountWhenNoPersonsWithStatus() throws Exception {
        // GIVEN
        when(personService.countByStatus(userId, PersonStatus.COMPLETED))
                .thenReturn(0L);

        // WHEN & THEN
        mockMvc.perform(get("/persons/stats/count")
                .param("status", "COMPLETED")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(content().string("0"));
    }
}
