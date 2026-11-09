package com.kurum.personel.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "personel_arabasi")
public class Personelarabasi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plaka", nullable = false)
    private String plaka;

    @Column(name = "marka", nullable = false)
    private String marka;

    @Column(name = "model", nullable = false)
    private String model;

    @Column(name = "arac_cikis_yili")
    private LocalDate aracCikisYili;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "personel_id", nullable = false)
    private Personel personel;

    protected Personelarabasi() {
    }

    public Personelarabasi(String plaka, String marka, String model, LocalDate aracCikisYili) {
        this.plaka = plaka;
        this.marka = marka;
        this.model = model;
        this.aracCikisYili = aracCikisYili;
    }

    public void update(String plaka, String marka, String model, LocalDate aracCikisYili) {
        this.plaka = plaka;
        this.marka = marka;
        this.model = model;
        this.aracCikisYili = aracCikisYili;
    }

    void setPersonel(Personel personel) {
        this.personel = personel;
    }

    public Long getId() { return id; }
    public String getPlaka() { return plaka; }
    public String getMarka() { return marka; }
    public String getModel() { return model; }
    public LocalDate getAracCikisYili() { return aracCikisYili; }
    public Personel getPersonel() { return personel; }
}
