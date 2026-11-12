package com.kurum.personel.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "garaj_kaydi")
public class GarajKaydi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "giris_tarihi", nullable = false)
    private LocalDate girisTarihi;

    @Column(name = "giris_saati", nullable = false)
    private LocalTime girisSaati;

    @Column(name = "cikis_tarihi")
    private LocalDate cikisTarihi;

    @Column(name = "cikis_saati")
    private LocalTime cikisSaati;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "personel_id", nullable = false)
    private Personel personel;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "araba_id", nullable = false)
    private Personelarabasi araba;

    protected GarajKaydi() {
    }

    public GarajKaydi(
            LocalDate girisTarihi,
            LocalTime girisSaati,
            LocalDate cikisTarihi,
            LocalTime cikisSaati,
            Personel personel,
            Personelarabasi araba) {
        update(girisTarihi, girisSaati, cikisTarihi, cikisSaati, personel, araba);
    }

    public void update(
            LocalDate girisTarihi,
            LocalTime girisSaati,
            LocalDate cikisTarihi,
            LocalTime cikisSaati,
            Personel personel,
            Personelarabasi araba) {
        this.girisTarihi = girisTarihi;
        this.girisSaati = girisSaati;
        this.cikisTarihi = cikisTarihi;
        this.cikisSaati = cikisSaati;
        this.personel = personel;
        this.araba = araba;
    }

    public Long getId() { return id; }
    public LocalDate getGirisTarihi() { return girisTarihi; }
    public LocalTime getGirisSaati() { return girisSaati; }
    public LocalDate getCikisTarihi() { return cikisTarihi; }
    public LocalTime getCikisSaati() { return cikisSaati; }
    public Personel getPersonel() { return personel; }
    public Personelarabasi getAraba() { return araba; }
}
