package com.kurum.personel.service;

import com.kurum.personel.dto.request.PersonelCreateRequest;
import com.kurum.personel.dto.request.PersonelUpdateRequest;
import com.kurum.personel.dto.response.PersonelResponse;
import com.kurum.personel.entity.Personel;
import com.kurum.personel.exception.BusinessException;
import com.kurum.personel.exception.ResourceNotFoundException;
import com.kurum.personel.repository.PersonelRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PersonelService {

    private final PersonelRepository personelRepository;

    public PersonelService(PersonelRepository personelRepository) {
        this.personelRepository = personelRepository;
    }

    @Transactional(readOnly = true)
    public PersonelResponse getById(Long id) {
        return PersonelResponse.from(findEntity(id));
    }

    @Transactional(readOnly = true)
    public List<PersonelResponse> getAll() {
        return personelRepository.findAll()
                .stream()
                .map(PersonelResponse::from)
                .toList();
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

        return PersonelResponse.from(personelRepository.save(personel));
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
        return PersonelResponse.from(personel);
    }

    @Transactional
    public void delete(Long id) {
        Personel personel = findEntity(id);
        personelRepository.delete(personel);
    }

    private Personel findEntity(Long id) {
        return personelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Personel bulunamadı. ID: " + id
                ));
    }
}
