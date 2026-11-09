package com.kurum.personel.dto.response;

import com.kurum.personel.entity.Personel;
import com.kurum.personel.dto.request.PersonelArabaFilter;

import java.time.LocalDate;
import java.util.List;

public record PersonelVeArabalariResponse(
        Long id,
        String ad,
        String soyad,
        String email,
        List<ArabaBilgisi> arabalar
) {
    public static PersonelVeArabalariResponse from(Personel personel) {
        return from(personel, null);
    }

    public static PersonelVeArabalariResponse from(Personel personel, PersonelArabaFilter filter) {
        List<ArabaBilgisi> arabalar = personel.getArabalar().stream()
                .filter(araba -> filter == null || !filter.hasArabaFilters() || filter.matchesAraba(araba))
                .map(araba -> new ArabaBilgisi(
                        araba.getId(),
                        araba.getPlaka(),
                        araba.getMarka(),
                        araba.getModel(),
                        araba.getAracCikisYili()
                ))
                .toList();

        return new PersonelVeArabalariResponse(
                personel.getId(),
                personel.getAd(),
                personel.getSoyad(),
                personel.getEmail(),
                arabalar
        );
    }

    public record ArabaBilgisi(
            Long id,
            String plaka,
            String marka,
            String model,
            LocalDate aracCikisYili
    ) {
    }
}
