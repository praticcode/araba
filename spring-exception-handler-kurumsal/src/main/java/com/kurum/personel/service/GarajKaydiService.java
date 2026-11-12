package com.kurum.personel.service;

import com.kurum.personel.dto.request.GarajKaydiRequest;
import com.kurum.personel.dto.response.GarajKaydiResponse;
import com.kurum.personel.entity.GarajKaydi;
import com.kurum.personel.entity.Personelarabasi;
import com.kurum.personel.exception.BusinessException;
import com.kurum.personel.exception.ResourceNotFoundException;
import com.kurum.personel.repository.ArabaRepository;
import com.kurum.personel.repository.GarajKaydiRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GarajKaydiService {

    private final GarajKaydiRepository garajKaydiRepository;
    private final ArabaRepository arabaRepository;

    public GarajKaydiService(
            GarajKaydiRepository garajKaydiRepository,
            ArabaRepository arabaRepository) {
        this.garajKaydiRepository = garajKaydiRepository;
        this.arabaRepository = arabaRepository;
    }

    @Transactional
    public GarajKaydiResponse create(GarajKaydiRequest request) {
        validateTimes(request);
        Personelarabasi araba = findAraba(request.arabaId());
        ensureArabaIsAvailable(araba.getId(), null, request);

        return GarajKaydiResponse.from(garajKaydiRepository.save(new GarajKaydi(
                request.girisTarihi(),
                request.girisSaati(),
                request.cikisTarihi(),
                request.cikisSaati(),
                araba.getPersonel(),
                araba
        )));
    }

    @Transactional(readOnly = true)
    public List<GarajKaydiResponse> getAll() {
        return garajKaydiRepository.findAllByOrderByGirisTarihiDescGirisSaatiDesc()
                .stream()
                .map(GarajKaydiResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public GarajKaydiResponse getById(Long id) {
        return GarajKaydiResponse.from(findKayit(id));
    }

    @Transactional
    public GarajKaydiResponse update(Long id, GarajKaydiRequest request) {
        validateTimes(request);
        GarajKaydi kayit = findKayit(id);
        Personelarabasi araba = findAraba(request.arabaId());
        ensureArabaIsAvailable(araba.getId(), id, request);

        kayit.update(
                request.girisTarihi(),
                request.girisSaati(),
                request.cikisTarihi(),
                request.cikisSaati(),
                araba.getPersonel(),
                araba
        );
        return GarajKaydiResponse.from(kayit);
    }

    @Transactional
    public void delete(Long id) {
        garajKaydiRepository.delete(findKayit(id));
    }

    private void validateTimes(GarajKaydiRequest request) {
        boolean hasExitDate = request.cikisTarihi() != null;
        boolean hasExitTime = request.cikisSaati() != null;
        if (hasExitDate != hasExitTime) {
            throw new BusinessException(
                    "Çıkış tarihi ve saati birlikte girilmelidir.",
                    HttpStatus.BAD_REQUEST,
                    "GARAGE_EXIT_TIME_INCOMPLETE"
            );
        }

        if (hasExitDate && LocalDateTime.of(request.cikisTarihi(), request.cikisSaati())
                .isBefore(LocalDateTime.of(request.girisTarihi(), request.girisSaati()))) {
            throw new BusinessException(
                    "Çıkış zamanı giriş zamanından önce olamaz.",
                    HttpStatus.BAD_REQUEST,
                    "GARAGE_EXIT_BEFORE_ENTRY"
            );
        }
    }

    private void ensureArabaIsAvailable(Long arabaId, Long excludedKayitId, GarajKaydiRequest request) {
        boolean alreadyInside = excludedKayitId == null
                ? garajKaydiRepository.existsByAraba_IdAndCikisTarihiIsNull(arabaId)
                : garajKaydiRepository.existsByAraba_IdAndCikisTarihiIsNullAndIdNot(arabaId, excludedKayitId);
        if (request.cikisTarihi() == null && alreadyInside) {
            throw new BusinessException(
                    "Bu araç için garajda açık bir giriş kaydı zaten var.",
                    HttpStatus.CONFLICT,
                    "VEHICLE_ALREADY_IN_GARAGE"
            );
        }
    }

    private GarajKaydi findKayit(Long id) {
        return garajKaydiRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Garaj kaydı bulunamadı. ID: " + id));
    }

    private Personelarabasi findAraba(Long id) {
        return arabaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Araç bulunamadı. ID: " + id));
    }
}
