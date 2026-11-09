package com.kurum.personel.dto.response;

import com.kurum.personel.entity.Personel;

public record PersonelResponse(Long id, String ad, String soyad, String email) {

    public static PersonelResponse from(Personel personel) {
        return new PersonelResponse(
                personel.getId(),
                personel.getAd(),
                personel.getSoyad(),
                personel.getEmail()
        );
    }
}
