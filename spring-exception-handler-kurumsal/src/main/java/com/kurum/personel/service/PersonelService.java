package com.kurum.personel.service;

import com.kurum.personel.dto.request.PersonelCreateRequest;
import com.kurum.personel.dto.request.PersonelArabaFilter;
import com.kurum.personel.dto.request.PersonelUpdateRequest;
import com.kurum.personel.dto.response.PersonelResponse;
import com.kurum.personel.dto.response.PersonelVeArabalariResponse;
import com.kurum.personel.entity.Personel;
import com.kurum.personel.entity.Personelarabasi;
import com.kurum.personel.exception.BusinessException;
import com.kurum.personel.exception.ResourceNotFoundException;
import com.kurum.personel.repository.PersonelRepository;
import com.kurum.personel.repository.GarajKaydiRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.util.List;

@Service
public class PersonelService {

    private static final Logger log = LoggerFactory.getLogger(PersonelService.class);

    private final PersonelRepository personelRepository;
    private final GarajKaydiRepository garajKaydiRepository;

    public PersonelService(PersonelRepository personelRepository, GarajKaydiRepository garajKaydiRepository) {
        this.personelRepository = personelRepository;
        this.garajKaydiRepository = garajKaydiRepository;
    }

    @Transactional(readOnly = true)
    public PersonelResponse getById(Long id) {
        log.debug("Personel getiriliyor. personelId={}", id);
        return PersonelResponse.from(findEntity(id));
    }

    @Transactional(readOnly = true)
    public List<PersonelResponse> getAll() {
        List<PersonelResponse> personeller = personelRepository.findAll()
                .stream()
                .map(PersonelResponse::from)
                .toList();
        log.debug("Personeller listelendi. personelSayisi={}", personeller.size());
        return personeller;
    }

    @Transactional(readOnly = true)
    public List<PersonelVeArabalariResponse> getAllWithArabalar() {
        List<PersonelVeArabalariResponse> personeller = personelRepository.findAllWithArabalarBy()
                .stream()
                .map(PersonelVeArabalariResponse::from)
                .toList();
        log.debug("Personel ve araçları listelendi. personelSayisi={}", personeller.size());
        return personeller;
    }

    @Transactional(readOnly = true)
    public List<PersonelVeArabalariResponse> search(PersonelArabaFilter filter) {
        List<PersonelVeArabalariResponse> personeller = personelRepository.findAll((root, query, criteriaBuilder) -> {
                    query.distinct(true);
                    var predicates = new java.util.ArrayList<Predicate>();

                    if (filter.id() != null) {
                        predicates.add(criteriaBuilder.equal(root.get("id"), filter.id()));
                    }
                    addTextFilter(predicates, criteriaBuilder, root.get("ad"), filter.ad());
                    addTextFilter(predicates, criteriaBuilder, root.get("soyad"), filter.soyad());
                    addTextFilter(predicates, criteriaBuilder, root.get("email"), filter.email());

                    if (filter.hasArabaFilters()) {
                        Join<Personel, Personelarabasi> araba = root.join("arabalar", JoinType.INNER);
                        if (filter.arabaId() != null) {
                            predicates.add(criteriaBuilder.equal(araba.get("id"), filter.arabaId()));
                        }
                        addTextFilter(predicates, criteriaBuilder, araba.get("plaka"), filter.plaka());
                        addTextFilter(predicates, criteriaBuilder, araba.get("marka"), filter.marka());
                        addTextFilter(predicates, criteriaBuilder, araba.get("model"), filter.model());
                        if (filter.aracCikisYili() != null) {
                            predicates.add(criteriaBuilder.equal(
                                    araba.get("aracCikisYili"), filter.aracCikisYili()
                            ));
                        }
                    }

                    return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
                })
                .stream()
                .map(personel -> PersonelVeArabalariResponse.from(personel, filter))
                .toList();
        log.debug("Personel araması tamamlandı. sonucSayisi={}", personeller.size());
        return personeller;
    }

    @Transactional
    public PersonelResponse create(PersonelCreateRequest request) {
        if (personelRepository.existsByEmail(request.email())) {
            throw new BusinessException(
                    "Bu e-posta adresiyle kayıtlı personel zaten var.",
                    HttpStatus.CONFLICT,
                    "PERSONEL_EMAIL_ALREADY_EXISTS"
            );
        }

        Personel personel = new Personel(
                request.ad(), request.soyad(), request.email()
        );

        Personel savedPersonel = personelRepository.save(personel);
        log.info("Personel oluşturuldu. personelId={}", savedPersonel.getId());
        return PersonelResponse.from(savedPersonel);
    }

    @Transactional
    public PersonelResponse update(Long id, PersonelUpdateRequest request) {
        Personel personel = findEntity(id);

        if (personelRepository.existsByEmailAndIdNot(request.email(), id)) {
            throw new BusinessException(
                    "Bu e-posta adresi başka bir personele ait.",
                    HttpStatus.CONFLICT,
                    "PERSONEL_EMAIL_ALREADY_EXISTS"
            );
        }

        personel.update(request.ad(), request.soyad(), request.email());
        log.info("Personel güncellendi. personelId={}", personel.getId());
        return PersonelResponse.from(personel);
    }

    @Transactional
    public void delete(Long id) {
        Personel personel = findEntity(id);
        if (!personel.getArabalar().isEmpty()) {
            throw new BusinessException(
                    "Arabası bulunan personel silinemez. Önce personelin arabalarını silin.",
                    HttpStatus.CONFLICT,
                    "PERSONEL_HAS_CARS"
            );
        }
        if (garajKaydiRepository.existsByPersonel_Id(id)) {
            throw new BusinessException(
                    "Garaj geçmişi bulunan personel silinemez.",
                    HttpStatus.CONFLICT,
                    "PERSONEL_HAS_GARAGE_RECORDS"
            );
        }
        personelRepository.delete(personel);
        log.info("Personel silindi. personelId={}", id);
    }

    private Personel  findEntity(Long id) {
        return personelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Personel bulunamadı. ID: " + id));
    }

    private void addTextFilter(
            List<Predicate> predicates,
            jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder,
            jakarta.persistence.criteria.Path<String> field,
            String filter) {
        if (filter != null && !filter.isBlank()) {
            String escaped = filter.trim().toLowerCase()
                    .replace("\\", "\\\\")
                    .replace("%", "\\%")
                    .replace("_", "\\_");
            predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(field),
                    "%" + escaped + "%",
                    '\\'
            ));
        }
    }
}
