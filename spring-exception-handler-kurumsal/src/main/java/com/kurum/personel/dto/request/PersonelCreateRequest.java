package com.kurum.personel.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PersonelCreateRequest(
        @NotBlank(message = "Ad alanı boş olamaz.")
        @Size(max = 100, message = "Ad en fazla 100 karakter olabilir.")
        String ad,

        @NotBlank(message = "Soyad alanı boş olamaz.")
        @Size(max = 100, message = "Soyad en fazla 100 karakter olabilir.")
        String soyad,

        @NotBlank(message = "E-posta alanı boş olamaz.")
        @Email(message = "Geçerli bir e-posta adresi giriniz.")
        @Size(max = 150, message = "E-posta en fazla 150 karakter olabilir.")
        String email
) {
}
