package org.iu.presentmanager.gifts;

import org.iu.presentmanager.config.WebConfig;
import org.iu.presentmanager.exceptions.ResourceNotFoundException;
import org.iu.presentmanager.giftIdeas.GiftIdea;
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

import java.time.LocalDate;
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

@WebMvcTest(GiftController.class)
@ActiveProfiles("test")
@Import({SecurityConfig.class, WebConfig.class})
class GiftControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GiftService giftService;

    @MockitoBean
    private SupabaseJwtConverter supabaseJwtConverter;

    private final UUID userId     = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private final UUID personId   = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private final UUID occasionId = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private final UUID giftId     = UUID.fromString("00000000-0000-0000-0000-000000000004");
    private final UUID giftIdeaId = UUID.fromString("00000000-0000-0000-0000-000000000005");

    private Gift testGift;

    @BeforeEach
    void setUp() {
        testGift = createTestGift("Test Gift", GiftStatus.PLANNED);
    }

    private Gift createTestGift(String title, GiftStatus status) {
        Gift gift = new Gift();
        gift.setId(giftId);
        gift.setUserId(userId);
        gift.setPersonId(personId);
        gift.setOccasionId(occasionId);
        gift.setTitle(title);
        gift.setStatus(status);
        return gift;
    }

    @Test
    void shouldCreateGiftAndReturnCreatedStatus() throws Exception {
        // GIVEN
        Gift newGift = new Gift();
        newGift.setPersonId(personId);
        newGift.setOccasionId(occasionId);
        newGift.setTitle("New Gift");

        when(giftService.createGift(any(Gift.class), eq(userId))).thenReturn(testGift);

        // WHEN & THEN
        mockMvc.perform(post("/gifts")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newGift)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(giftId.toString()))
                .andExpect(jsonPath("$.title").value("Test Gift"))
                .andExpect(jsonPath("$.status").value("PLANNED"));

        verify(giftService).createGift(any(Gift.class), eq(userId));
    }

    @Test
    void shouldReturnBadRequestWhenCreatingGiftWithMissingTitle() throws Exception {
        // GIVEN — title fehlt (@NotBlank)
        String invalidGiftJson = "{\"personId\":\"" + personId + "\",\"occasionId\":\"" + occasionId + "\"}";

        // WHEN & THEN
        mockMvc.perform(post("/gifts")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON)
                        .content(invalidGiftJson))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        verify(giftService, never()).createGift(any(), any());
    }

    @Test
    void shouldReturnBadRequestWhenCreatingGiftWithMissingPersonId() throws Exception {
        // GIVEN — personId fehlt (@NotNull)
        String invalidGiftJson = "{\"title\":\"Gift\",\"occasionId\":\"" + occasionId + "\"}";

        // WHEN & THEN
        mockMvc.perform(post("/gifts")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON)
                        .content(invalidGiftJson))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        verify(giftService, never()).createGift(any(), any());
    }

    @Test
    void shouldReturnNotFoundWhenPersonDoesNotExistOnCreate() throws Exception {
        // GIVEN
        Gift newGift = new Gift();
        newGift.setPersonId(personId);
        newGift.setOccasionId(occasionId);
        newGift.setTitle("Gift");

        when(giftService.createGift(any(Gift.class), eq(userId)))
                .thenThrow(new ResourceNotFoundException("Person not found with id: " + personId));

        // WHEN & THEN
        mockMvc.perform(post("/gifts")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newGift)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound());

        verify(giftService).createGift(any(Gift.class), eq(userId));
    }

    @Test
    void shouldCreateGiftFromGiftIdeaAndReturnCreatedStatus() throws Exception {
        // GIVEN
        GiftIdea giftIdea = new GiftIdea();
        giftIdea.setId(giftIdeaId);
        giftIdea.setPersonId(personId);
        giftIdea.setOccasionId(occasionId);
        giftIdea.setTitle("Test Gift Idea");

        when(giftService.createGiftFromGiftIdea(any(GiftIdea.class), eq(userId))).thenReturn(testGift);

        // WHEN & THEN
        mockMvc.perform(post("/gifts/gift-idea")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(giftIdea)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(giftId.toString()))
                .andExpect(jsonPath("$.title").value("Test Gift"));

        verify(giftService).createGiftFromGiftIdea(any(GiftIdea.class), eq(userId));
    }

    @Test
    void shouldReturnNotFoundWhenGiftIdeaDoesNotExistOnCreate() throws Exception {
        // GIVEN
        GiftIdea giftIdea = new GiftIdea();
        giftIdea.setId(giftIdeaId);
        giftIdea.setPersonId(personId);
        giftIdea.setOccasionId(occasionId);
        giftIdea.setTitle("Ghost Idea");

        when(giftService.createGiftFromGiftIdea(any(GiftIdea.class), eq(userId)))
                .thenThrow(new ResourceNotFoundException("Gift idea not found with id: " + giftIdeaId));

        // WHEN & THEN
        mockMvc.perform(post("/gifts/gift-idea")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(giftIdea)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound());

        verify(giftService).createGiftFromGiftIdea(any(GiftIdea.class), eq(userId));
    }

    @Test
    void shouldReturnAllGiftsWhenNoFilterProvided() throws Exception {
        // GIVEN
        when(giftService.getAllGiftsByUser(userId)).thenReturn(List.of(testGift));

        // WHEN & THEN
        mockMvc.perform(get("/gifts")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(giftId.toString()))
                .andExpect(jsonPath("$[0].title").value("Test Gift"));

        verify(giftService).getAllGiftsByUser(userId);
        verifyNoMoreInteractions(giftService);
    }

    @Test
    void shouldReturnEmptyListWhenNoGiftsExist() throws Exception {
        // GIVEN
        when(giftService.getAllGiftsByUser(userId)).thenReturn(Collections.emptyList());

        // WHEN & THEN
        mockMvc.perform(get("/gifts")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(giftService).getAllGiftsByUser(userId);
    }

    @Test
    void shouldFilterGiftsByPersonId() throws Exception {
        // GIVEN
        when(giftService.getGiftsByPerson(userId, personId)).thenReturn(List.of(testGift));

        // WHEN & THEN
        mockMvc.perform(get("/gifts")
                        .param("personId", personId.toString())
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(giftService).getGiftsByPerson(userId, personId);
        verify(giftService, never()).getAllGiftsByUser(any());
    }

    @Test
    void shouldFilterGiftsByOccasionId() throws Exception {
        // GIVEN
        when(giftService.getGiftsByOccasion(userId, occasionId)).thenReturn(List.of(testGift));

        // WHEN & THEN
        mockMvc.perform(get("/gifts")
                        .param("occasionId", occasionId.toString())
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(giftService).getGiftsByOccasion(userId, occasionId);
    }

    @Test
    void shouldFilterGiftsByStatus() throws Exception {
        // GIVEN
        when(giftService.getGiftsByStatus(userId, GiftStatus.PLANNED)).thenReturn(List.of(testGift));

        // WHEN & THEN
        mockMvc.perform(get("/gifts")
                        .param("status", "PLANNED")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("PLANNED"));

        verify(giftService).getGiftsByStatus(userId, GiftStatus.PLANNED);
    }

    @Test
    void shouldFilterGiftsByPersonAndOccasion() throws Exception {
        // GIVEN
        when(giftService.getGiftsByPersonAndOccasion(userId, personId, occasionId))
                .thenReturn(List.of(testGift));

        // WHEN & THEN
        mockMvc.perform(get("/gifts")
                        .param("personId", personId.toString())
                        .param("occasionId", occasionId.toString())
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(giftService).getGiftsByPersonAndOccasion(userId, personId, occasionId);
    }

    @Test
    void shouldFilterGiftsByPersonAndStatus() throws Exception {
        // GIVEN
        when(giftService.getGiftsByPersonAndStatus(userId, personId, GiftStatus.PLANNED))
                .thenReturn(List.of(testGift));

        // WHEN & THEN
        mockMvc.perform(get("/gifts")
                        .param("personId", personId.toString())
                        .param("status", "PLANNED")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(giftService).getGiftsByPersonAndStatus(userId, personId, GiftStatus.PLANNED);
    }

    @Test
    void shouldFilterGiftsByGiftIdeaId() throws Exception {
        // GIVEN
        when(giftService.getGiftsByGiftIdea(userId, giftIdeaId)).thenReturn(List.of(testGift));

        // WHEN & THEN
        mockMvc.perform(get("/gifts")
                        .param("giftIdeaId", giftIdeaId.toString())
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(giftService).getGiftsByGiftIdea(userId, giftIdeaId);
    }

    @Test
    void shouldReturnGiftById() throws Exception {
        // GIVEN
        when(giftService.getGiftById(giftId, userId)).thenReturn(testGift);

        // WHEN & THEN
        mockMvc.perform(get("/gifts/{id}", giftId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(giftId.toString()))
                .andExpect(jsonPath("$.title").value("Test Gift"));

        verify(giftService).getGiftById(giftId, userId);
    }

    @Test
    void shouldReturnNotFoundWhenGiftDoesNotExist() throws Exception {
        // GIVEN
        when(giftService.getGiftById(giftId, userId))
                .thenThrow(new ResourceNotFoundException("Gift not found with id: " + giftId));

        // WHEN & THEN
        mockMvc.perform(get("/gifts/{id}", giftId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound());

        verify(giftService).getGiftById(giftId, userId);
    }

    @Test
    void shouldSearchGiftsAndReturnMatches() throws Exception {
        // GIVEN
        when(giftService.searchGifts(userId, "Test")).thenReturn(List.of(testGift));

        // WHEN & THEN
        mockMvc.perform(get("/gifts/search")
                        .param("query", "Test")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Test Gift"));

        verify(giftService).searchGifts(userId, "Test");
    }

    @Test
    void shouldReturnEmptyListWhenSearchYieldsNoResults() throws Exception {
        // GIVEN
        when(giftService.searchGifts(userId, "NonExistent")).thenReturn(Collections.emptyList());

        // WHEN & THEN
        mockMvc.perform(get("/gifts/search")
                        .param("query", "NonExistent")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(giftService).searchGifts(userId, "NonExistent");
    }

    @Test
    void shouldReturnNotPurchasedGifts() throws Exception {
        // GIVEN
        when(giftService.getNotPurchasedGifts(userId)).thenReturn(List.of(testGift));

        // WHEN & THEN
        mockMvc.perform(get("/gifts/not-purchased")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(giftService).getNotPurchasedGifts(userId);
    }

    @Test
    void shouldReturnEmptyListWhenAllGiftsPurchased() throws Exception {
        // GIVEN
        when(giftService.getNotPurchasedGifts(userId)).thenReturn(Collections.emptyList());

        // WHEN & THEN
        mockMvc.perform(get("/gifts/not-purchased")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(giftService).getNotPurchasedGifts(userId);
    }

    @Test
    void shouldReturnNotGivenGifts() throws Exception {
        // GIVEN
        when(giftService.getNotGivenGifts(userId)).thenReturn(List.of(testGift));

        // WHEN & THEN
        mockMvc.perform(get("/gifts/not-given")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(giftService).getNotGivenGifts(userId);
    }

    @Test
    void shouldReturnEmptyListWhenAllGiftsGiven() throws Exception {
        // GIVEN
        when(giftService.getNotGivenGifts(userId)).thenReturn(Collections.emptyList());

        // WHEN & THEN
        mockMvc.perform(get("/gifts/not-given")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(giftService).getNotGivenGifts(userId);
    }

    @Test
    void shouldReturnRecentGifts() throws Exception {
        // GIVEN
        when(giftService.getRecentGifts(userId)).thenReturn(List.of(testGift));

        // WHEN & THEN
        mockMvc.perform(get("/gifts/recent")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(giftService).getRecentGifts(userId);
    }

    @Test
    void shouldReturnGiftsSortedByWorkflowOrder() throws Exception {
        // GIVEN
        Gift plannedGift = createTestGift("Planned Gift", GiftStatus.PLANNED);
        Gift boughtGift  = createTestGift("Bought Gift",  GiftStatus.BOUGHT);
        Gift giftedGift  = createTestGift("Gifted Gift",  GiftStatus.GIFTED);
        when(giftService.getGiftsByWorkflowOrder(userId))
                .thenReturn(List.of(plannedGift, boughtGift, giftedGift));

        // WHEN & THEN
        mockMvc.perform(get("/gifts/workflow-order")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].status").value("PLANNED"))
                .andExpect(jsonPath("$[1].status").value("BOUGHT"))
                .andExpect(jsonPath("$[2].status").value("GIFTED"));

        verify(giftService).getGiftsByWorkflowOrder(userId);
    }

    @Test
    void shouldReturnTrueWhenGiftIdeaIsAlreadyUsed() throws Exception {
        // GIVEN
        when(giftService.isGiftIdeaAlreadyUsed(userId, giftIdeaId)).thenReturn(true);

        // WHEN & THEN
        mockMvc.perform(get("/gifts/check-gift-idea/{giftIdeaId}", giftIdeaId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(giftService).isGiftIdeaAlreadyUsed(userId, giftIdeaId);
    }

    @Test
    void shouldReturnFalseWhenGiftIdeaIsNotYetUsed() throws Exception {
        // GIVEN
        when(giftService.isGiftIdeaAlreadyUsed(userId, giftIdeaId)).thenReturn(false);

        // WHEN & THEN
        mockMvc.perform(get("/gifts/check-gift-idea/{giftIdeaId}", giftIdeaId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(giftService).isGiftIdeaAlreadyUsed(userId, giftIdeaId);
    }

    @Test
    void shouldUpdateGiftSuccessfully() throws Exception {
        // GIVEN
        Gift updatedGift = createTestGift("Updated Gift", GiftStatus.BOUGHT);

        when(giftService.updateGift(eq(giftId), eq(userId), any(Gift.class))).thenReturn(updatedGift);

        // WHEN & THEN
        mockMvc.perform(put("/gifts/{id}", giftId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedGift)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Gift"))
                .andExpect(jsonPath("$.status").value("BOUGHT"));

        verify(giftService).updateGift(eq(giftId), eq(userId), any(Gift.class));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentGift() throws Exception {
        // GIVEN
        Gift updatedGift = createTestGift("Updated Gift", GiftStatus.BOUGHT);

        when(giftService.updateGift(eq(giftId), eq(userId), any(Gift.class)))
                .thenThrow(new ResourceNotFoundException("Gift not found with id: " + giftId));

        // WHEN & THEN
        mockMvc.perform(put("/gifts/{id}", giftId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedGift)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound());

        verify(giftService).updateGift(eq(giftId), eq(userId), any(Gift.class));
    }

    @Test
    void shouldUpdateGiftStatusToBought() throws Exception {
        // GIVEN
        Gift boughtGift = createTestGift("Test Gift", GiftStatus.BOUGHT);
        boughtGift.setPurchaseDate(LocalDate.now());
        when(giftService.updateGiftStatus(giftId, userId, GiftStatus.BOUGHT)).thenReturn(boughtGift);

        // WHEN & THEN
        mockMvc.perform(patch("/gifts/{id}/status", giftId)
                        .param("status", "BOUGHT")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BOUGHT"));

        verify(giftService).updateGiftStatus(giftId, userId, GiftStatus.BOUGHT);
    }

    @Test
    void shouldUpdateGiftStatusToGifted() throws Exception {
        // GIVEN
        Gift giftedGift = createTestGift("Test Gift", GiftStatus.GIFTED);
        giftedGift.setGivenDate(LocalDate.now());
        when(giftService.updateGiftStatus(giftId, userId, GiftStatus.GIFTED)).thenReturn(giftedGift);

        // WHEN & THEN
        mockMvc.perform(patch("/gifts/{id}/status", giftId)
                        .param("status", "GIFTED")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("GIFTED"));

        verify(giftService).updateGiftStatus(giftId, userId, GiftStatus.GIFTED);
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingStatusOfNonExistentGift() throws Exception {
        // GIVEN
        when(giftService.updateGiftStatus(giftId, userId, GiftStatus.BOUGHT))
                .thenThrow(new ResourceNotFoundException("Gift not found with id: " + giftId));

        // WHEN & THEN
        mockMvc.perform(patch("/gifts/{id}/status", giftId)
                        .param("status", "BOUGHT")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound());

        verify(giftService).updateGiftStatus(giftId, userId, GiftStatus.BOUGHT);
    }

    @Test
    void shouldDeleteGiftAndReturnNoContent() throws Exception {
        // GIVEN
        doNothing().when(giftService).deleteGift(giftId, userId);

        // WHEN & THEN
        mockMvc.perform(delete("/gifts/{id}", giftId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNoContent());

        verify(giftService).deleteGift(giftId, userId);
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentGift() throws Exception {
        // GIVEN
        doThrow(new ResourceNotFoundException("Gift not found with id: " + giftId))
                .when(giftService).deleteGift(giftId, userId);

        // WHEN & THEN
        mockMvc.perform(delete("/gifts/{id}", giftId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound());

        verify(giftService).deleteGift(giftId, userId);
    }
}