package com.kurum.personel.dto.response;

import com.kurum.personel.entity.GarajKaydi;

import java.time.LocalDate;
import java.time.LocalTime;

public record GarajKaydiResponse(
        Long id,
        LocalDate girisTarihi,
        LocalTime girisSaati,
        LocalDate cikisTarihi,
        LocalTime cikisSaati,
        Long personelId,
        String personelAd,
        String personelSoyad,
        Long arabaId,
        String plaka,
        String marka,
        String model
) {
    public static GarajKaydiResponse from(GarajKaydi kayit) {
        return new GarajKaydiResponse(
                kayit.getId(),
                kayit.getGirisTarihi(),
                kayit.getGirisSaati(),
                kayit.getCikisTarihi(),
                kayit.getCikisSaati(),
                kayit.getPersonel().getId(),
                kayit.getPersonel().getAd(),
                kayit.getPersonel().getSoyad(),
                kayit.getAraba().getId(),
                kayit.getAraba().getPlaka(),
                kayit.getAraba().getMarka(),
                kayit.getAraba().getModel()
        );
    }
}
