package org.iu.presentmanager.giftIdeas;

import org.iu.presentmanager.config.WebConfig;
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

@WebMvcTest(GiftIdeaController.class)
@ActiveProfiles("test")
@Import({SecurityConfig.class, WebConfig.class})
class GiftIdeaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GiftIdeaService giftIdeaService;

    @MockitoBean
    private SupabaseJwtConverter supabaseJwtConverter;

    private final UUID userId     = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private final UUID personId   = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private final UUID occasionId = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private final UUID giftIdeaId = UUID.fromString("00000000-0000-0000-0000-000000000004");

    private GiftIdea testGiftIdea;

    @BeforeEach
    void setUp() {
        testGiftIdea = createTestGiftIdea("Test Gift Idea", GiftSource.MANUAL);
    }

    private GiftIdea createTestGiftIdea(String title, GiftSource source) {
        GiftIdea giftIdea = new GiftIdea();
        giftIdea.setId(giftIdeaId);
        giftIdea.setUserId(userId);
        giftIdea.setPersonId(personId);
        giftIdea.setOccasionId(occasionId);
        giftIdea.setTitle(title);
        giftIdea.setSource(source);
        return giftIdea;
    }

    @Test
    void shouldCreateGiftIdeaAndReturnCreatedStatus() throws Exception {
        // GIVEN
        GiftIdea newGiftIdea = new GiftIdea();
        newGiftIdea.setPersonId(personId);
        newGiftIdea.setOccasionId(occasionId);
        newGiftIdea.setTitle("New Gift Idea");

        when(giftIdeaService.createGiftIdea(any(GiftIdea.class), eq(userId))).thenReturn(testGiftIdea);

        // WHEN & THEN
        mockMvc.perform(post("/gift-ideas")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newGiftIdea)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(giftIdeaId.toString()))
                .andExpect(jsonPath("$.title").value("Test Gift Idea"))
                .andExpect(jsonPath("$.source").value("MANUAL"));

        verify(giftIdeaService).createGiftIdea(any(GiftIdea.class), eq(userId));
    }

    @Test
    void shouldReturnBadRequestWhenCreatingGiftIdeaWithMissingTitle() throws Exception {
        // GIVEN — title fehlt (@NotBlank)
        String invalidJson = "{\"personId\":\"" + personId + "\",\"occasionId\":\"" + occasionId + "\"}";

        // WHEN & THEN
        mockMvc.perform(post("/gift-ideas")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON)
                        .content(invalidJson))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        verify(giftIdeaService, never()).createGiftIdea(any(), any());
    }

    @Test
    void shouldReturnBadRequestWhenCreatingGiftIdeaWithMissingPersonId() throws Exception {
        // GIVEN — personId fehlt (@NotNull)
        String invalidJson = "{\"title\":\"Idea\",\"occasionId\":\"" + occasionId + "\"}";

        // WHEN & THEN
        mockMvc.perform(post("/gift-ideas")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON)
                        .content(invalidJson))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        verify(giftIdeaService, never()).createGiftIdea(any(), any());
    }

    @Test
    void shouldReturnNotFoundWhenPersonDoesNotExistOnCreate() throws Exception {
        // GIVEN
        GiftIdea newGiftIdea = new GiftIdea();
        newGiftIdea.setPersonId(personId);
        newGiftIdea.setOccasionId(occasionId);
        newGiftIdea.setTitle("Idea");

        when(giftIdeaService.createGiftIdea(any(GiftIdea.class), eq(userId)))
                .thenThrow(new ResourceNotFoundException("Person not found with id: " + personId));

        // WHEN & THEN
        mockMvc.perform(post("/gift-ideas")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newGiftIdea)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound());

        verify(giftIdeaService).createGiftIdea(any(GiftIdea.class), eq(userId));
    }

    @Test
    void shouldReturnNotFoundWhenOccasionDoesNotExistOnCreate() throws Exception {
        // GIVEN
        GiftIdea newGiftIdea = new GiftIdea();
        newGiftIdea.setPersonId(personId);
        newGiftIdea.setOccasionId(occasionId);
        newGiftIdea.setTitle("Idea");

        when(giftIdeaService.createGiftIdea(any(GiftIdea.class), eq(userId)))
                .thenThrow(new ResourceNotFoundException("Occasion not found with id: " + occasionId));

        // WHEN & THEN
        mockMvc.perform(post("/gift-ideas")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newGiftIdea)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound());

        verify(giftIdeaService).createGiftIdea(any(GiftIdea.class), eq(userId));
    }

    @Test
    void shouldReturnAllGiftIdeasWhenNoFilterProvided() throws Exception {
        // GIVEN
        when(giftIdeaService.getAllGiftIdeasByUser(userId)).thenReturn(List.of(testGiftIdea));

        // WHEN & THEN
        mockMvc.perform(get("/gift-ideas")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(giftIdeaId.toString()))
                .andExpect(jsonPath("$[0].title").value("Test Gift Idea"));

        verify(giftIdeaService).getAllGiftIdeasByUser(userId);
        verifyNoMoreInteractions(giftIdeaService);
    }

    @Test
    void shouldReturnEmptyListWhenNoGiftIdeasExist() throws Exception {
        // GIVEN
        when(giftIdeaService.getAllGiftIdeasByUser(userId)).thenReturn(Collections.emptyList());

        // WHEN & THEN
        mockMvc.perform(get("/gift-ideas")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(giftIdeaService).getAllGiftIdeasByUser(userId);
    }

    @Test
    void shouldFilterGiftIdeasByPersonId() throws Exception {
        // GIVEN
        when(giftIdeaService.getGiftIdeasByPerson(userId, personId)).thenReturn(List.of(testGiftIdea));

        // WHEN & THEN
        mockMvc.perform(get("/gift-ideas")
                        .param("personId", personId.toString())
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(giftIdeaService).getGiftIdeasByPerson(userId, personId);
        verify(giftIdeaService, never()).getAllGiftIdeasByUser(any());
    }

    @Test
    void shouldFilterGiftIdeasByOccasionId() throws Exception {
        // GIVEN
        when(giftIdeaService.getGiftIdeasByOccasion(userId, occasionId)).thenReturn(List.of(testGiftIdea));

        // WHEN & THEN
        mockMvc.perform(get("/gift-ideas")
                        .param("occasionId", occasionId.toString())
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(giftIdeaService).getGiftIdeasByOccasion(userId, occasionId);
    }

    @Test
    void shouldFilterGiftIdeasBySource() throws Exception {
        // GIVEN
        when(giftIdeaService.getGiftIdeasBySource(userId, GiftSource.AI))
                .thenReturn(List.of(createTestGiftIdea("AI Idea", GiftSource.AI)));

        // WHEN & THEN
        mockMvc.perform(get("/gift-ideas")
                        .param("source", "AI")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].source").value("AI"));

        verify(giftIdeaService).getGiftIdeasBySource(userId, GiftSource.AI);
    }

    @Test
    void shouldFilterGiftIdeasByPersonAndOccasion() throws Exception {
        // GIVEN
        when(giftIdeaService.getGiftIdeasByPersonAndOccasion(userId, personId, occasionId))
                .thenReturn(List.of(testGiftIdea));

        // WHEN & THEN
        mockMvc.perform(get("/gift-ideas")
                        .param("personId", personId.toString())
                        .param("occasionId", occasionId.toString())
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(giftIdeaService).getGiftIdeasByPersonAndOccasion(userId, personId, occasionId);
    }

    @Test
    void shouldReturnGiftIdeaById() throws Exception {
        // GIVEN
        when(giftIdeaService.getGiftIdeaById(giftIdeaId, userId)).thenReturn(testGiftIdea);

        // WHEN & THEN
        mockMvc.perform(get("/gift-ideas/{id}", giftIdeaId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(giftIdeaId.toString()))
                .andExpect(jsonPath("$.title").value("Test Gift Idea"));

        verify(giftIdeaService).getGiftIdeaById(giftIdeaId, userId);
    }

    @Test
    void shouldReturnNotFoundWhenGiftIdeaDoesNotExist() throws Exception {
        // GIVEN
        when(giftIdeaService.getGiftIdeaById(giftIdeaId, userId))
                .thenThrow(new ResourceNotFoundException("Gift idea not found with id: " + giftIdeaId));

        // WHEN & THEN
        mockMvc.perform(get("/gift-ideas/{id}", giftIdeaId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound());

        verify(giftIdeaService).getGiftIdeaById(giftIdeaId, userId);
    }

    @Test
    void shouldSearchGiftIdeasAndReturnMatches() throws Exception {
        // GIVEN
        when(giftIdeaService.searchGiftIdeas(userId, "Test")).thenReturn(List.of(testGiftIdea));

        // WHEN & THEN
        mockMvc.perform(get("/gift-ideas/search")
                        .param("query", "Test")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Test Gift Idea"));

        verify(giftIdeaService).searchGiftIdeas(userId, "Test");
    }

    @Test
    void shouldReturnEmptyListWhenSearchYieldsNoResults() throws Exception {
        // GIVEN
        when(giftIdeaService.searchGiftIdeas(userId, "NonExistent")).thenReturn(Collections.emptyList());

        // WHEN & THEN
        mockMvc.perform(get("/gift-ideas/search")
                        .param("query", "NonExistent")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(giftIdeaService).searchGiftIdeas(userId, "NonExistent");
    }

    @Test
    void shouldReturnRecentGiftIdeas() throws Exception {
        // GIVEN
        when(giftIdeaService.getRecentGiftIdeas(userId)).thenReturn(List.of(testGiftIdea));

        // WHEN & THEN
        mockMvc.perform(get("/gift-ideas/recent")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(giftIdeaService).getRecentGiftIdeas(userId);
    }

    @Test
    void shouldReturnEmptyListWhenNoRecentGiftIdeas() throws Exception {
        // GIVEN
        when(giftIdeaService.getRecentGiftIdeas(userId)).thenReturn(Collections.emptyList());

        // WHEN & THEN
        mockMvc.perform(get("/gift-ideas/recent")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(giftIdeaService).getRecentGiftIdeas(userId);
    }

    @Test
    void shouldUpdateGiftIdeaSuccessfully() throws Exception {
        // GIVEN
        GiftIdea updatedGiftIdea = createTestGiftIdea("Updated Idea", GiftSource.MANUAL);

        when(giftIdeaService.updateGiftIdea(eq(giftIdeaId), eq(userId), any(GiftIdea.class)))
                .thenReturn(updatedGiftIdea);

        // WHEN & THEN
        mockMvc.perform(put("/gift-ideas/{id}", giftIdeaId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedGiftIdea)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Idea"));

        verify(giftIdeaService).updateGiftIdea(eq(giftIdeaId), eq(userId), any(GiftIdea.class));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentGiftIdea() throws Exception {
        // GIVEN
        GiftIdea updatedGiftIdea = createTestGiftIdea("Updated Idea", GiftSource.MANUAL);

        when(giftIdeaService.updateGiftIdea(eq(giftIdeaId), eq(userId), any(GiftIdea.class)))
                .thenThrow(new ResourceNotFoundException("Gift idea not found with id: " + giftIdeaId));

        // WHEN & THEN
        mockMvc.perform(put("/gift-ideas/{id}", giftIdeaId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedGiftIdea)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound());

        verify(giftIdeaService).updateGiftIdea(eq(giftIdeaId), eq(userId), any(GiftIdea.class));
    }

    @Test
    void shouldReturnBadRequestWhenPersonChangedOnUpdate() throws Exception {
        // GIVEN
        GiftIdea updatedGiftIdea = createTestGiftIdea("Updated Idea", GiftSource.MANUAL);

        when(giftIdeaService.updateGiftIdea(eq(giftIdeaId), eq(userId), any(GiftIdea.class)))
                .thenThrow(new IllegalArgumentException("Person cannot be changed for existing gift idea"));

        // WHEN & THEN
        mockMvc.perform(put("/gift-ideas/{id}", giftIdeaId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedGiftIdea)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        verify(giftIdeaService).updateGiftIdea(eq(giftIdeaId), eq(userId), any(GiftIdea.class));
    }

    @Test
    void shouldDeleteGiftIdeaAndReturnNoContent() throws Exception {
        // GIVEN
        doNothing().when(giftIdeaService).deleteGiftIdea(giftIdeaId, userId);

        // WHEN & THEN
        mockMvc.perform(delete("/gift-ideas/{id}", giftIdeaId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNoContent());

        verify(giftIdeaService).deleteGiftIdea(giftIdeaId, userId);
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentGiftIdea() throws Exception {
        // GIVEN
        doThrow(new ResourceNotFoundException("Gift idea not found with id: " + giftIdeaId))
                .when(giftIdeaService).deleteGiftIdea(giftIdeaId, userId);

        // WHEN & THEN
        mockMvc.perform(delete("/gift-ideas/{id}", giftIdeaId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound());

        verify(giftIdeaService).deleteGiftIdea(giftIdeaId, userId);
    }
}