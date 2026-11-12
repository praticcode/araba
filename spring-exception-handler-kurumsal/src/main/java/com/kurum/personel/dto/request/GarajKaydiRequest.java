package com.kurum.personel.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record GarajKaydiRequest(
        @NotNull(message = "Giriş tarihi boş olamaz.")
        LocalDate girisTarihi,

        @NotNull(message = "Giriş saati boş olamaz.")
        LocalTime girisSaati,

        LocalDate cikisTarihi,

        LocalTime cikisSaati,

        @NotNull(message = "Araç seçimi boş olamaz.")
        Long arabaId
) {
}
