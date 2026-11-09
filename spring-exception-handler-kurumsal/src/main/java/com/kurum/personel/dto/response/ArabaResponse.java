package com.kurum.personel.dto.response;

import com.kurum.personel.entity.Personelarabasi;

import java.time.LocalDate;

public record ArabaResponse(
        Long id,
        String plaka,
        String marka,
        String model,
        LocalDate aracCikisYili,
        Long personelId
) {
    public static ArabaResponse from(Personelarabasi araba) {
        return new ArabaResponse(
                araba.getId(),
                araba.getPlaka(),
                araba.getMarka(),
                araba.getModel(),
                araba.getAracCikisYili(),
                araba.getPersonel().getId()
        );
    }
}
