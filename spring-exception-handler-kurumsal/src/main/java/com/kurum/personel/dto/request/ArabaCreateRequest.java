package com.kurum.personel.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ArabaCreateRequest(
        @NotBlank(message = "Plaka alanı boş olamaz.")
        @Size(max = 20, message = "Plaka en fazla 20 karakter olabilir.")
        String plaka,

        @NotBlank(message = "Marka alanı boş olamaz.")
        @Size(max = 100, message = "Marka en fazla 100 karakter olabilir.")
        String marka,

        @NotBlank(message = "Model alanı boş olamaz.")
        @Size(max = 100, message = "Model en fazla 100 karakter olabilir.")
        String model,

        LocalDate aracCikisYili,

        @NotNull(message = "Personel ID alanı boş olamaz.")
        Long personelId
) {
}
