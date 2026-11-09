package com.kurum.personel.dto.request;

import com.kurum.personel.entity.Personelarabasi;

import java.time.LocalDate;

public record PersonelArabaFilter(
        Long id,
        String ad,
        String soyad,
        String email,
        Long arabaId,
        String plaka,
        String marka,
        String model,
        LocalDate aracCikisYili
) {
    public boolean hasArabaFilters() {
        return arabaId != null
                || hasText(plaka)
                || hasText(marka)
                || hasText(model)
                || aracCikisYili != null;
    }

    public boolean matchesAraba(Personelarabasi araba) {
        return (arabaId == null || arabaId.equals(araba.getId()))
                && matchesText(plaka, araba.getPlaka())
                && matchesText(marka, araba.getMarka())
                && matchesText(model, araba.getModel())
                && (aracCikisYili == null || aracCikisYili.equals(araba.getAracCikisYili()));
    }

    private static boolean matchesText(String filter, String value) {
        return !hasText(filter)
                || value.toLowerCase().contains(filter.trim().toLowerCase());
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
