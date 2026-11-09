package com.kurum.personel.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "personel", uniqueConstraints = {
        @UniqueConstraint(name = "uk_personel_email", columnNames = "email")
})
public class Personel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String ad;

    @Column(nullable = false, length = 100)
    private String soyad;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @OneToMany(mappedBy = "personel")
    private List<Personelarabasi> arabalar = new ArrayList<>();

    protected Personel() {
    }

    public Personel(String ad, String soyad, String email) {
        this.ad = ad;
        this.soyad = soyad;
        this.email = email;
    }

    public void update(String ad, String soyad, String email) {
        this.ad = ad;
        this.soyad = soyad;
        this.email = email;
    }

    public Long getId() { return id; }
    public String getAd() { return ad; }
    public String getSoyad() { return soyad; }
    public String getEmail() { return email; }

    public List<Personelarabasi> getArabalar() {
        return Collections.unmodifiableList(arabalar);
    }

    public void addAraba(Personelarabasi araba) {
        if (!arabalar.contains(araba)) {
            arabalar.add(araba);
            araba.setPersonel(this);
        }
    }

    public void removeAraba(Personelarabasi araba) {
        if (arabalar.remove(araba)) {
            araba.setPersonel(null);
        }
    }
}
