package com.kurum.personel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kurum.personel.dto.request.ArabaCreateRequest;
import com.kurum.personel.dto.request.ArabaUpdateRequest;
import com.kurum.personel.entity.Personel;
import com.kurum.personel.repository.ArabaRepository;
import com.kurum.personel.repository.PersonelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ArabaControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ArabaRepository arabaRepository;

    @Autowired
    PersonelRepository personelRepository;

    @BeforeEach
    void setUp() {
        arabaRepository.deleteAll();
        personelRepository.deleteAll();
    }

    @Test
    void arabaCrudIslemleriYapilabilmeli() throws Exception {
        Personel personel = personelRepository.save(
                new Personel("Ayşe", "Demir", "ayse.araba@example.com")
        );

        String createRequest = objectMapper.writeValueAsString(new ArabaCreateRequest(
                "34ABC123", "Toyota", "Corolla", LocalDate.of(2022, 1, 1), personel.getId()
        ));

        String response = mockMvc.perform(post("/api/arabalar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.plaka").value("34ABC123"))
                .andExpect(jsonPath("$.personelId").value(personel.getId()))
                .andReturn()
                .getResponse()
                .getContentAsString();
        long arabaId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/arabalar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(arabaId));

        mockMvc.perform(get("/api/arabalar/{id}", arabaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.marka").value("Toyota"));

        String updateRequest = objectMapper.writeValueAsString(new ArabaUpdateRequest(
                "34XYZ456", "Honda", "Civic", LocalDate.of(2023, 1, 1), personel.getId()
        ));
        mockMvc.perform(put("/api/arabalar/{id}", arabaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plaka").value("34XYZ456"))
                .andExpect(jsonPath("$.marka").value("Honda"));

        mockMvc.perform(delete("/api/arabalar/{id}", arabaId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/arabalar/{id}", arabaId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void bulunmayanPersonelIdIleArabaOlusturulamamali() throws Exception {
        String createRequest = objectMapper.writeValueAsString(new ArabaCreateRequest(
                "34ABC123", "Toyota", "Corolla", null, 999L
        ));

        mockMvc.perform(post("/api/arabalar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequest))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void personelVeArabalariBirlikteListelenebilmeli() throws Exception {
        Personel personel = personelRepository.save(
                new Personel("Ayşe", "Demir", "ayse.detay@example.com")
        );
        String createRequest = objectMapper.writeValueAsString(new ArabaCreateRequest(
                "34ABC123", "Toyota", "Corolla", LocalDate.of(2022, 1, 1), personel.getId()
        ));
        mockMvc.perform(post("/api/arabalar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequest))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/personeller/detayli"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(personel.getId()))
                .andExpect(jsonPath("$[0].ad").value("Ayşe"))
                .andExpect(jsonPath("$[0].arabalar[0].plaka").value("34ABC123"))
                .andExpect(jsonPath("$[0].arabalar[0].marka").value("Toyota"));
    }

    @Test
    void personelVeArabaFiltreleriAndIleUygulanipYalnizcaUyanArabalarDonmeli() throws Exception {
        Personel personel = personelRepository.save(
                new Personel("Ayşe", "Demir", "ayse.filtre@example.com")
        );
        String toyotaRequest = objectMapper.writeValueAsString(new ArabaCreateRequest(
                "34ABC123", "Toyota", "Corolla", LocalDate.of(2022, 1, 1), personel.getId()
        ));
        String hondaRequest = objectMapper.writeValueAsString(new ArabaCreateRequest(
                "34XYZ456", "Honda", "Civic", LocalDate.of(2023, 1, 1), personel.getId()
        ));
        mockMvc.perform(post("/api/arabalar").contentType(MediaType.APPLICATION_JSON).content(toyotaRequest))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/arabalar").contentType(MediaType.APPLICATION_JSON).content(hondaRequest))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/personeller/ara")
                        .param("ad", "ayşe")
                        .param("marka", "toy")
                        .param("model", "cor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].arabalar.length()").value(1))
                .andExpect(jsonPath("$[0].arabalar[0].marka").value("Toyota"));

        mockMvc.perform(get("/api/personeller/ara")
                        .param("ad", "Ayşe")
                        .param("marka", "Toyota")
                        .param("plaka", "XYZ"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void arabasiOlanPersonelSilinememeli() throws Exception {
        Personel personel = personelRepository.save(
                new Personel("Mehmet", "Kaya", "mehmet.araba@example.com")
        );
        String createRequest = objectMapper.writeValueAsString(new ArabaCreateRequest(
                "06DEF789", "Ford", "Focus", null, personel.getId()
        ));
        mockMvc.perform(post("/api/arabalar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequest))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/personeller/{id}", personel.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PERSONEL_HAS_CARS"));
    }
}
