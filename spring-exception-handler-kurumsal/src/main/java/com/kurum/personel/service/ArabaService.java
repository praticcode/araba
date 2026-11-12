package com.kurum.personel.service;

import com.kurum.personel.dto.request.ArabaCreateRequest;
import com.kurum.personel.dto.request.ArabaUpdateRequest;
import com.kurum.personel.dto.response.ArabaResponse;
import com.kurum.personel.entity.Personel;
import com.kurum.personel.entity.Personelarabasi;
import com.kurum.personel.exception.BusinessException;
import com.kurum.personel.exception.ResourceNotFoundException;
import com.kurum.personel.repository.ArabaRepository;
import com.kurum.personel.repository.GarajKaydiRepository;
import com.kurum.personel.repository.PersonelRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ArabaService {

    private final ArabaRepository arabaRepository;
    private final PersonelRepository personelRepository;
    private final GarajKaydiRepository garajKaydiRepository;

    public ArabaService(
            ArabaRepository arabaRepository,
            PersonelRepository personelRepository,
            GarajKaydiRepository garajKaydiRepository) {
        this.arabaRepository = arabaRepository;
        this.personelRepository = personelRepository;
        this.garajKaydiRepository = garajKaydiRepository;
    }

    @Transactional
    public ArabaResponse create(ArabaCreateRequest request) {
        Personel personel = findPersonel(request.personelId());
        Personelarabasi araba = new Personelarabasi(
                request.plaka(), request.marka(), request.model(), request.aracCikisYili()
        );
        personel.addAraba(araba);
        return ArabaResponse.from(arabaRepository.save(araba));
    }

    @Transactional(readOnly = true)
    public List<ArabaResponse> getAll() {
        return arabaRepository.findAll()
                .stream()
                .map(ArabaResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ArabaResponse getById(Long id) {
        return ArabaResponse.from(findAraba(id));
    }

    @Transactional
    public ArabaResponse update(Long id, ArabaUpdateRequest request) {
        Personelarabasi araba = findAraba(id);
        Personel yeniPersonel = findPersonel(request.personelId());

        araba.update(request.plaka(), request.marka(), request.model(), request.aracCikisYili());

        if (!araba.getPersonel().getId().equals(yeniPersonel.getId())) {
            araba.getPersonel().removeAraba(araba);
            yeniPersonel.addAraba(araba);
        }

        return ArabaResponse.from(araba);
    }

    @Transactional
    public void delete(Long id) {
        Personelarabasi araba = findAraba(id);
        if (garajKaydiRepository.existsByAraba_Id(id)) {
            throw new BusinessException(
                    "Garaj geçmişi bulunan araç silinemez.",
                    HttpStatus.CONFLICT,
                    "ARABA_HAS_GARAGE_RECORDS"
            );
        }
        araba.getPersonel().removeAraba(araba);
        arabaRepository.delete(araba);
    }

    private Personelarabasi findAraba(Long id) {
        return arabaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Araba bulunamadı. ID: " + id));
    }

    private Personel findPersonel(Long id) {
        return personelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Personel bulunamadı. ID: " + id));
    }
}
