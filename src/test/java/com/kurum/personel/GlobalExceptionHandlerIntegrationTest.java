package com.kurum.personel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kurum.personel.dto.request.PersonelCreateRequest;
import com.kurum.personel.repository.ArabaRepository;
import com.kurum.personel.repository.PersonelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class GlobalExceptionHandlerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired ArabaRepository arabaRepository;
    @Autowired PersonelRepository personelRepository;

    @BeforeEach
    void setUp() {
        arabaRepository.deleteAll();
        personelRepository.deleteAll();
    }

    @Test
    void bulunamayanPersonel404Donmeli() throws Exception {
        mockMvc.perform(get("/api/personeller/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.path").value("/api/personeller/999"));
    }

    @Test
    void validationHatasi400Donmeli() throws Exception {
        String json = """
                {"ad":"","soyad":"Yılmaz","email":"gecersiz"}
                """;

        mockMvc.perform(post("/api/personeller")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.validationErrors.ad").exists())
                .andExpect(jsonPath("$.validationErrors.email").exists());
    }

    @Test
    void ayniEmailIkinciKezEklenirse409Donmeli() throws Exception {
        String json = objectMapper.writeValueAsString(
                new PersonelCreateRequest("Ayşe", "Demir", "ayse@example.com")
        );

        mockMvc.perform(post("/api/personeller")
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/personeller")
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PERSONEL_EMAIL_ALREADY_EXISTS"));
    }
}
