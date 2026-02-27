package org.iu.presentmanager.occasions;

import org.iu.presentmanager.config.WebConfig;
import org.iu.presentmanager.exceptions.DuplicateResourceException;
import org.iu.presentmanager.exceptions.ResourceNotFoundException;
import org.iu.presentmanager.security.SecurityConfig;
import org.iu.presentmanager.security.SupabaseJwtConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import tools.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OccasionController.class)
@ActiveProfiles("test")
@Import({SecurityConfig.class, WebConfig.class})
class OccasionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OccasionService occasionService;

    @MockitoBean
    private SupabaseJwtConverter supabaseJwtConverter;

    private final UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private final UUID occasionId = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private Occasion testOccasion;

    @BeforeEach
    void setUp() {
        testOccasion = createTestOccasion("Christmas", OccasionType.FIXED, 12, 25);
    }

    private Occasion createTestOccasion(String name, OccasionType type, Integer month, Integer day) {
        Occasion occasion = new Occasion();
        occasion.setId(occasionId);
        occasion.setName(name);
        occasion.setUserId(userId);
        occasion.setType(type);
        occasion.setFixedMonth(month);
        occasion.setFixedDay(day);
        occasion.setIsRecurring(true);
        return occasion;
    }

    @Test
    void shouldReturnAllOccasions() throws Exception {
        // GIVEN
        when(occasionService.getAllOccasionsByUser(userId))
                .thenReturn(List.of(testOccasion));

        // WHEN & THEN
        mockMvc.perform(get("/occasions")
                .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(occasionId.toString()))
                .andExpect(jsonPath("$[0].name").value("Christmas"))
                .andExpect(jsonPath("$[0].type").value("FIXED"));

        verify(occasionService).getAllOccasionsByUser(userId);
        verifyNoMoreInteractions(occasionService);
    }

    @Test
    void shouldReturnEmptyListWhenNoOccasions() throws Exception {
        // GIVEN
        when(occasionService.getAllOccasionsByUser(userId))
                .thenReturn(Collections.emptyList());

        // WHEN & THEN
        mockMvc.perform(get("/occasions")
                .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(occasionService).getAllOccasionsByUser(userId);
    }

    @Test
    void shouldFilterOccasionsByType() throws Exception {
        // GIVEN
        when(occasionService.getOccasionsByType(userId, OccasionType.FIXED))
                .thenReturn(List.of(testOccasion));

        // WHEN & THEN
        mockMvc.perform(get("/occasions")
                .param("type", "FIXED")
                .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].type").value("FIXED"));

        verify(occasionService).getOccasionsByType(userId, OccasionType.FIXED);
        verify(occasionService, never()).getAllOccasionsByUser(any());
    }

    @Test
    void shouldFilterOccasionsByRecurring() throws Exception {
        // GIVEN
        when(occasionService.getRecurringOccasions(userId, true))
                .thenReturn(List.of(testOccasion));

        // WHEN & THEN
        mockMvc.perform(get("/occasions")
                .param("isRecurring", "true")
                .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(occasionService).getRecurringOccasions(userId, true);
    }

    @Test
    void shouldReturnOccasionById() throws Exception {
        // GIVEN
        when(occasionService.getOccasionById(occasionId, userId))
                .thenReturn(testOccasion);

        // WHEN & THEN
        mockMvc.perform(get("/occasions/{id}", occasionId)
                .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(occasionId.toString()))
                .andExpect(jsonPath("$.name").value("Christmas"));

        verify(occasionService).getOccasionById(occasionId, userId);
    }

    @Test
    void shouldReturnNotFoundWhenOccasionDoesNotExist() throws Exception {
        // GIVEN
        when(occasionService.getOccasionById(occasionId, userId))
                .thenThrow(new ResourceNotFoundException("Occasion not found with id: " + occasionId));

        // WHEN & THEN
        mockMvc.perform(get("/occasions/{id}", occasionId)
                .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound());

        verify(occasionService).getOccasionById(occasionId, userId);
    }

    @Test
    void shouldCreateOccasion() throws Exception {
        // GIVEN
        Occasion newOccasion = new Occasion();
        newOccasion.setName("Birthday");
        newOccasion.setType(OccasionType.FIXED);
        newOccasion.setFixedMonth(6);
        newOccasion.setFixedDay(15);
        newOccasion.setIsRecurring(true);

        Occasion createdOccasion = createTestOccasion("Birthday", OccasionType.FIXED, 6, 15);
        when(occasionService.createOccasion(any(Occasion.class), eq(userId)))
                .thenReturn(createdOccasion);

        // WHEN & THEN
        mockMvc.perform(post("/occasions")
                .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newOccasion)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Birthday"))
                .andExpect(jsonPath("$.type").value("FIXED"));

        verify(occasionService).createOccasion(any(Occasion.class), eq(userId));
    }

    @Test
    void shouldReturnBadRequestForInvalidOccasion() throws Exception {
        // GIVEN
        String invalidOccasionJson = "{\"type\":\"FIXED\"}"; // Name fehlt

        // WHEN & THEN
        mockMvc.perform(post("/occasions")
                .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                .contentType(APPLICATION_JSON)
                .content(invalidOccasionJson))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        verify(occasionService, never()).createOccasion(any(), any());
    }

    @Test
    void shouldReturnConflictForDuplicateOccasion() throws Exception {
        // GIVEN
        Occasion newOccasion = new Occasion();
        newOccasion.setName("Christmas");
        newOccasion.setType(OccasionType.FIXED);
        newOccasion.setFixedMonth(12);
        newOccasion.setFixedDay(25);

        when(occasionService.createOccasion(any(Occasion.class), eq(userId)))
                .thenThrow(new DuplicateResourceException("Occasion already exists with name: Christmas"));

        // WHEN & THEN
        mockMvc.perform(post("/occasions")
                .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newOccasion)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isConflict());

        verify(occasionService).createOccasion(any(Occasion.class), eq(userId));
    }

    @Test
    void shouldUpdateOccasion() throws Exception {
        // GIVEN
        Occasion updatedOccasion = createTestOccasion("New Year", OccasionType.FIXED, 1, 1);
        when(occasionService.updateOccasion(eq(occasionId), eq(userId), any(Occasion.class)))
                .thenReturn(updatedOccasion);

        // WHEN & THEN
        mockMvc.perform(put("/occasions/{id}", occasionId)
                .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedOccasion)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Year"))
                .andExpect(jsonPath("$.fixedMonth").value(1))
                .andExpect(jsonPath("$.fixedDay").value(1));

        verify(occasionService).updateOccasion(eq(occasionId), eq(userId), any(Occasion.class));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentOccasion() throws Exception {
        // GIVEN
        Occasion updatedOccasion = createTestOccasion("Updated", OccasionType.FIXED, 1, 1);
        when(occasionService.updateOccasion(eq(occasionId), eq(userId), any(Occasion.class)))
                .thenThrow(new ResourceNotFoundException("Occasion not found with id: " + occasionId));

        // WHEN & THEN
        mockMvc.perform(put("/occasions/{id}", occasionId)
                .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedOccasion)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound());

        verify(occasionService).updateOccasion(eq(occasionId), eq(userId), any(Occasion.class));
    }

    @Test
    void shouldDeleteOccasion() throws Exception {
        // GIVEN
        doNothing().when(occasionService).deleteOccasion(occasionId, userId);

        // WHEN & THEN
        mockMvc.perform(delete("/occasions/{id}", occasionId)
                .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNoContent());

        verify(occasionService).deleteOccasion(occasionId, userId);
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentOccasion() throws Exception {
        // GIVEN
        doThrow(new ResourceNotFoundException("Occasion not found with id: " + occasionId))
                .when(occasionService).deleteOccasion(occasionId, userId);

        // WHEN & THEN
        mockMvc.perform(delete("/occasions/{id}", occasionId)
                .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound());

        verify(occasionService).deleteOccasion(occasionId, userId);
    }

    @Test
    void shouldSearchOccasionsByName() throws Exception {
        // GIVEN
        when(occasionService.searchOccasionsByName(userId, "Christ"))
                .thenReturn(List.of(testOccasion));

        // WHEN & THEN
        mockMvc.perform(get("/occasions/search")
                .param("query", "Christ")
                .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Christmas"));

        verify(occasionService).searchOccasionsByName(userId, "Christ");
    }

    @Test
    void shouldReturnEmptyListWhenSearchDoesNotMatch() throws Exception {
        // GIVEN
        when(occasionService.searchOccasionsByName(userId, "NonExistent"))
                .thenReturn(Collections.emptyList());

        // WHEN & THEN
        mockMvc.perform(get("/occasions/search")
                .param("query", "NonExistent")
                .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(occasionService).searchOccasionsByName(userId, "NonExistent");
    }

    @Test
    void shouldGetFixedOccasionsByMonth() throws Exception {
        // GIVEN
        when(occasionService.getFixedOccasionsByMonth(userId, 12))
                .thenReturn(List.of(testOccasion));

        // WHEN & THEN
        mockMvc.perform(get("/occasions/fixed/month/{month}", 12)
                .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].fixedMonth").value(12));

        verify(occasionService).getFixedOccasionsByMonth(userId, 12);
    }

    @Test
    void shouldReturnBadRequestForInvalidMonth() throws Exception {
        // GIVEN
        when(occasionService.getFixedOccasionsByMonth(userId, 13))
                .thenThrow(new IllegalArgumentException("Month must be between 1 and 12"));

        // WHEN & THEN
        mockMvc.perform(get("/occasions/fixed/month/{month}", 13)
                .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        verify(occasionService).getFixedOccasionsByMonth(userId, 13);
    }

    @Test
    void shouldGetTodayFixedOccasions() throws Exception {
        // GIVEN
        when(occasionService.getTodayFixedOccasions(userId))
                .thenReturn(List.of(testOccasion));

        // WHEN & THEN
        mockMvc.perform(get("/occasions/fixed/today")
                .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(occasionService).getTodayFixedOccasions(userId);
    }

    @Test
    void shouldReturnEmptyListWhenNoTodayOccasions() throws Exception {
        // GIVEN
        when(occasionService.getTodayFixedOccasions(userId))
                .thenReturn(Collections.emptyList());

        // WHEN & THEN
        mockMvc.perform(get("/occasions/fixed/today")
                .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(occasionService).getTodayFixedOccasions(userId);
    }

    @Test
    void shouldGetRecurringFixedOccasions() throws Exception {
        // GIVEN
        when(occasionService.getRecurringFixedOccasions(userId))
                .thenReturn(List.of(testOccasion));

        // WHEN & THEN
        mockMvc.perform(get("/occasions/fixed/recurring")
                .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(occasionService).getRecurringFixedOccasions(userId);
    }

    @Test
    void shouldCountAllOccasions() throws Exception {
        // GIVEN
        when(occasionService.countOccasionsByUser(userId)).thenReturn(5L);

        // WHEN & THEN
        mockMvc.perform(get("/occasions/stats/count")
                .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(content().string("5"));

        verify(occasionService).countOccasionsByUser(userId);
    }

    @Test
    void shouldCountOccasionsByType() throws Exception {
        // GIVEN
        when(occasionService.countOccasionsByType(userId, OccasionType.FIXED)).thenReturn(3L);

        // WHEN & THEN
        mockMvc.perform(get("/occasions/stats/count")
                .param("type", "FIXED")
                .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(content().string("3"));

        verify(occasionService).countOccasionsByType(userId, OccasionType.FIXED);
    }

    @Test
    void shouldCheckOccasionExists() throws Exception {
        // GIVEN
        when(occasionService.existsByName(userId, "Christmas")).thenReturn(true);

        // WHEN & THEN
        mockMvc.perform(get("/occasions/exists")
                .param("name", "Christmas")
                .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(occasionService).existsByName(userId, "Christmas");
    }

    @Test
    void shouldReturnFalseWhenOccasionNameDoesNotExist() throws Exception {
        // GIVEN
        when(occasionService.existsByName(userId, "NonExistent")).thenReturn(false);

        // WHEN & THEN
        mockMvc.perform(get("/occasions/exists")
                .param("name", "NonExistent")
                .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(occasionService).existsByName(userId, "NonExistent");
    }
}
