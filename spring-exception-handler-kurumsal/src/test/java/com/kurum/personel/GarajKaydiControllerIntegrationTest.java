package com.kurum.personel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kurum.personel.entity.GarajKaydi;
import com.kurum.personel.entity.Personel;
import com.kurum.personel.entity.Personelarabasi;
import com.kurum.personel.repository.ArabaRepository;
import com.kurum.personel.repository.GarajKaydiRepository;
import com.kurum.personel.repository.PersonelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.nullValue;

@SpringBootTest
@AutoConfigureMockMvc
class GarajKaydiControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    GarajKaydiRepository garajKaydiRepository;

    @Autowired
    ArabaRepository arabaRepository;

    @Autowired
    PersonelRepository personelRepository;

    @BeforeEach
    void setUp() {
        garajKaydiRepository.deleteAll();
        arabaRepository.deleteAll();
        personelRepository.deleteAll();
    }

    @Test
    void garajGirisCikisKaydiOlusturulupGuncellenebilmeli() throws Exception {
        Personel personel = new Personel("Ayşe", "Demir", "ayse.garaj@example.com");
        Personelarabasi araba = new Personelarabasi("34ABC123", "Toyota", "Corolla", null);
        personel.addAraba(araba);
        personelRepository.save(personel);
        arabaRepository.save(araba);

        String createRequest = requestJson(araba.getId(),
                "2026-11-12", "08:30:00", null, null);
        String response = mockMvc.perform(post("/api/garaj-kayitlari")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.girisTarihi").value("2026-11-12"))
                .andExpect(jsonPath("$.girisSaati").value("08:30:00"))
                .andExpect(jsonPath("$.cikisTarihi").value(nullValue()))
                .andExpect(jsonPath("$.personelAd").value("Ayşe"))
                .andExpect(jsonPath("$.personelId").value(personel.getId()))
                .andExpect(jsonPath("$.plaka").value("34ABC123"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        long kayitId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/garaj-kayitlari"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(kayitId));

        mockMvc.perform(put("/api/garaj-kayitlari/{id}", kayitId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson(araba.getId(),
                                "2026-11-12", "08:30:00", "2026-11-12", "17:45:00")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cikisTarihi").value("2026-11-12"))
                .andExpect(jsonPath("$.cikisSaati").value("17:45:00"));

        mockMvc.perform(get("/api/garaj-kayitlari/{id}", kayitId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cikisSaati").value("17:45:00"));

        mockMvc.perform(delete("/api/garaj-kayitlari/{id}", kayitId))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/garaj-kayitlari/{id}", kayitId))
                .andExpect(status().isNotFound());
    }

    @Test
    void acikGarajKaydiOlanAracIcinIkinciGirisOlusturulamamali() throws Exception {
        Personel personel = createPersonelAndCar("acik-kayit");
        Personelarabasi araba = arabaRepository.findAll().get(0);
        String request = requestJson(araba.getId(),
                "2026-11-12", "08:30:00", null, null);

        mockMvc.perform(post("/api/garaj-kayitlari")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/garaj-kayitlari")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("VEHICLE_ALREADY_IN_GARAGE"));
    }

    @Test
    void cikisTarihiVeSaatiBirlikteVerilmeliVeCikisSonraOlmali() throws Exception {
        Personel personel = createPersonelAndCar("cikis-dogrulama");
        Personelarabasi araba = arabaRepository.findAll().get(0);

        mockMvc.perform(post("/api/garaj-kayitlari")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson(araba.getId(),
                                "2026-11-12", "08:30:00", "2026-11-12", null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("GARAGE_EXIT_TIME_INCOMPLETE"));

        mockMvc.perform(post("/api/garaj-kayitlari")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson(araba.getId(),
                                "2026-11-12", "08:30:00", "2026-11-12", "08:29:00")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("GARAGE_EXIT_BEFORE_ENTRY"));
    }

    @Test
    void gecmisiOlanAracSilinememeli() throws Exception {
        Personel personel = createPersonelAndCar("silme-koruma");
        Personelarabasi araba = arabaRepository.findAll().get(0);
        garajKaydiRepository.save(new GarajKaydi(
                LocalDate.of(2026, 11, 12),
                LocalTime.of(8, 30),
                LocalDate.of(2026, 11, 12),
                LocalTime.of(17, 0),
                personel,
                araba
        ));

        mockMvc.perform(delete("/api/arabalar/{id}", araba.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ARABA_HAS_GARAGE_RECORDS"));
    }

    private Personel createPersonelAndCar(String suffix) {
        Personel personel = new Personel("Mehmet", "Kaya", suffix + "@garaj.example.com");
        Personelarabasi araba = new Personelarabasi("06DEF789", "Ford", "Focus", null);
        personel.addAraba(araba);
        personelRepository.save(personel);
        arabaRepository.save(araba);
        return personel;
    }

    private String requestJson(
            Long arabaId,
            String girisTarihi,
            String girisSaati,
            String cikisTarihi,
            String cikisSaati) throws Exception {
        return objectMapper.writeValueAsString(new Request(
                LocalDate.parse(girisTarihi),
                LocalTime.parse(girisSaati),
                cikisTarihi == null ? null : LocalDate.parse(cikisTarihi),
                cikisSaati == null ? null : LocalTime.parse(cikisSaati),
                arabaId
        ));
    }

    private record Request(
            LocalDate girisTarihi,
            LocalTime girisSaati,
            LocalDate cikisTarihi,
            LocalTime cikisSaati,
            Long arabaId
    ) {
    }
}
