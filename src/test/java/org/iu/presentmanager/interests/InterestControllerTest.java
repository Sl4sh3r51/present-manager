package org.iu.presentmanager.interests;

import org.iu.presentmanager.config.UUIDPrincipalArgumentResolver;
import org.iu.presentmanager.config.WebConfig;
import org.iu.presentmanager.exceptions.DuplicateResourceException;
import org.iu.presentmanager.exceptions.ResourceNotFoundException;
import org.iu.presentmanager.security.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.MockMvcPrint;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InterestController.class)
@AutoConfigureMockMvc(print = MockMvcPrint.DEFAULT)
@ActiveProfiles("test")
@Import({SecurityConfig.class, WebConfig.class})
class InterestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InterestService interestService;

    @MockitoBean
    private UUIDPrincipalArgumentResolver uuidPrincipalArgumentResolver;

    private Interest testInterest;

    @BeforeEach
    public void setUp() {
        testInterest = new Interest();
        testInterest.setId(UUID.randomUUID());
        testInterest.setName("Test Interest");
    }

    @Test
    void shouldReturnAllInterests() throws Exception {
        //Given
        when(interestService.getAllInterests()).thenReturn(List.of(testInterest));

        //When & Then
        mockMvc.perform(get("/interests").with(jwt().jwt(builder -> builder.subject(UUID.randomUUID().toString())))
                .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value(testInterest.getName()));

        verify(interestService).getAllInterests();
        verifyNoMoreInteractions(interestService);
    }

    @Test
    void shouldReturnEmptyListWhenNoInterests() throws Exception {
        //Given
        when(interestService.getAllInterests()).thenReturn(List.of());

        //When & Then
        mockMvc.perform(get("/interests").with(jwt().jwt(builder -> builder.subject(UUID.randomUUID().toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(interestService).getAllInterests();
        verifyNoMoreInteractions(interestService);
    }

    @Test
    void shouldReturnInterestById() throws Exception {
        //Given
        when(interestService.getInterestById(testInterest.getId())).thenReturn(testInterest);

        //When & Then
        mockMvc.perform(get("/interests/{id}", testInterest.getId())
                        .with(jwt().jwt(builder -> builder.subject(UUID.randomUUID().toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(testInterest.getName()));

        verify(interestService).getInterestById(testInterest.getId());
        verifyNoMoreInteractions(interestService);
    }

    @Test
    void shouldReturnNotFoundForNonExistingInterest() throws Exception {
        //Given
        UUID nonExistingId = UUID.randomUUID();
        when(interestService.getInterestById(nonExistingId)).thenThrow(new ResourceNotFoundException("Interest not found with id: " + nonExistingId));

        //When & Then
        mockMvc.perform(get("/interests/{id}", nonExistingId)
                        .with(jwt().jwt(builder -> builder.subject(UUID.randomUUID().toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound());

        verify(interestService).getInterestById(nonExistingId);
        verifyNoMoreInteractions(interestService);
    }

    @Test
    void shouldReturnInterestByName() throws Exception {
        //Given
        when(interestService.getInterestByName(testInterest.getName())).thenReturn(testInterest);

        //When & Then
        mockMvc.perform(get("/interests/search")
                        .param("name", testInterest.getName())
                        .with(jwt().jwt(builder -> builder.subject(UUID.randomUUID().toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(testInterest.getName()));

        verify(interestService).getInterestByName(testInterest.getName());
        verifyNoMoreInteractions(interestService);
    }

    @Test
    void shouldReturnNotFoundForNonExistingInterestByName() throws Exception {
        //Given
        String nonExistingName = "Non Existing Interest";
        when(interestService.getInterestByName(nonExistingName)).thenThrow(new ResourceNotFoundException("Interest not found with name: " + nonExistingName));

        //When & Then
        mockMvc.perform(get("/interests/search")
                        .param("name", nonExistingName)
                        .with(jwt().jwt(builder -> builder.subject(UUID.randomUUID().toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound());

        verify(interestService).getInterestByName(nonExistingName);
        verifyNoMoreInteractions(interestService);
    }

    @Test
    void shouldReturnBadRequestForMissingNameParameter() throws Exception {
        //When & Then
        mockMvc.perform(get("/interests/search")
                        .with(jwt().jwt(builder -> builder.subject(UUID.randomUUID().toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        verifyNoInteractions(interestService);
    }

    @Test
    void shouldCreateNewInterest() throws Exception {
        //Given
        Interest newInterest = new Interest();
        newInterest.setName("New Interest");

        Interest createdInterest = new Interest();
        createdInterest.setId(UUID.randomUUID());
        createdInterest.setName(newInterest.getName());

        when(interestService.createInterest(any(Interest.class))).thenReturn(createdInterest);

        //When & Then
        mockMvc.perform(post("/interests")
                        .with(jwt().jwt(builder -> builder.subject(UUID.randomUUID().toString())))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newInterest)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(createdInterest.getId().toString()))
                .andExpect(jsonPath("$.name").value(createdInterest.getName()));

        verify(interestService).createInterest(any(Interest.class));
        verifyNoMoreInteractions(interestService);
    }

    @Test
    void shouldReturnDuplicateResourceWhenCreatingInterestWithExistingName() throws Exception {
        //Given
        Interest newInterest = new Interest();
        newInterest.setName(testInterest.getName());

        when(interestService.createInterest(any(Interest.class)))
                .thenThrow(new DuplicateResourceException("Interest already exists with name: " + testInterest.getName()));

        //When & Then
        mockMvc.perform(post("/interests")
                        .with(jwt().jwt(builder -> builder.subject(UUID.randomUUID().toString())))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newInterest)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isConflict());

        verify(interestService).createInterest(any(Interest.class));
        verifyNoMoreInteractions(interestService);
    }

    @Test
    void shouldReturnBadRequestWhenCreatingInterestWithMissingName() throws Exception {
        //Given
        Interest newInterest = new Interest();
        when(interestService.createInterest(any(Interest.class))).thenThrow(new IllegalArgumentException("Interest name is required"));

        //When & Then
        mockMvc.perform(post("/interests")
                        .with(jwt().jwt(builder -> builder.subject(UUID.randomUUID().toString())))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newInterest)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        verify(interestService).createInterest(any(Interest.class));
        verifyNoMoreInteractions(interestService);
    }

    @Test
    void shouldDeleteInterest() throws Exception {
        //Given
        UUID interestId = testInterest.getId();
        doNothing().when(interestService).deleteInterest(interestId);

        //When & Then
        mockMvc.perform(delete("/interests/{id}", interestId)
                        .with(jwt().jwt(builder -> builder.subject(UUID.randomUUID().toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNoContent());

        verify(interestService).deleteInterest(interestId);
        verifyNoMoreInteractions(interestService);
    }

    @Test
    void shouldReturnResourceNotFoundWhenDeletingNonExistingInterest() throws Exception {
        //Given
        UUID nonExistingId = UUID.randomUUID();
        doThrow(new ResourceNotFoundException("Interest not found with id: " + nonExistingId))
                .when(interestService).deleteInterest(nonExistingId);

        //When & Then
        mockMvc.perform(delete("/interests/{id}", nonExistingId)
                        .with(jwt().jwt(builder -> builder.subject(UUID.randomUUID().toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound());

        verify(interestService).deleteInterest(nonExistingId);
        verifyNoMoreInteractions(interestService);
    }

    @Test
    void shouldReturnTrueWhenInterestExists() throws Exception {
        //Given
        String existingName = testInterest.getName();
        when(interestService.existsByName(existingName)).thenReturn(true);

        //When & Then
        mockMvc.perform(get("/interests/exists")
                        .param("name", existingName)
                        .with(jwt().jwt(builder -> builder.subject(UUID.randomUUID().toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        verify(interestService).existsByName(existingName);
        verifyNoMoreInteractions(interestService);
    }

    @Test
    void shouldReturnFalseWhenInterestDoesNotExist() throws Exception {
        //Given
        String nonExistingName = "Non Existing Interest";
        when(interestService.existsByName(nonExistingName)).thenReturn(false);

        //When & Then
        mockMvc.perform(get("/interests/exists")
                        .param("name", nonExistingName)
                        .with(jwt().jwt(builder -> builder.subject(UUID.randomUUID().toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(false));

        verify(interestService).existsByName(nonExistingName);
        verifyNoMoreInteractions(interestService);
    }

    @Test
    void shouldReturnBadRequestWhenCheckingExistenceWithMissingNameParameter() throws Exception {
        //When & Then
        mockMvc.perform(get("/interests/exists")
                        .with(jwt().jwt(builder -> builder.subject(UUID.randomUUID().toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        verifyNoInteractions(interestService);
    }
}
