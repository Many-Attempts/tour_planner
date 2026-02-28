package org.example.tourplanner.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.tourplanner.dto.request.TourRequest;
import org.example.tourplanner.dto.response.TourResponse;
import org.example.tourplanner.model.TransportType;
import org.example.tourplanner.security.JwtTokenProvider;
import org.example.tourplanner.service.TourService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TourController.class)
@AutoConfigureMockMvc(addFilters = false)
class TourControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private TourService tourService;

    @MockitoBean private JwtTokenProvider jwtTokenProvider;
    @MockitoBean private UserDetailsService userDetailsService;

    @Test
    @WithMockUser
    void getAllTours_returns200() throws Exception {
        TourResponse response = TourResponse.builder().id(1L).name("Test Tour").build();
        when(tourService.getAllTours(any())).thenReturn(List.of(response));

        mockMvc.perform(get("/api/tours"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Tour"));
    }

    @Test
    @WithMockUser
    void getTourById_returns200() throws Exception {
        TourResponse response = TourResponse.builder().id(1L).name("Test Tour").build();
        when(tourService.getTourById(any(), any())).thenReturn(response);

        mockMvc.perform(get("/api/tours/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Tour"));
    }

    @Test
    @WithMockUser
    void createTour_returns200() throws Exception {
        TourRequest request = new TourRequest("New Tour", "Desc", "A", "B", TransportType.HIKING, null, null);
        TourResponse response = TourResponse.builder().id(1L).name("New Tour").build();
        when(tourService.createTour(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/tours")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Tour"));
    }
}
