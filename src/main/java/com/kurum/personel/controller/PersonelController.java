package com.kurum.personel.controller;

import com.kurum.personel.dto.request.PersonelCreateRequest;
import com.kurum.personel.dto.request.PersonelUpdateRequest;
import com.kurum.personel.dto.response.PersonelResponse;
import com.kurum.personel.dto.response.PersonelVeArabalariResponse;
import com.kurum.personel.service.PersonelService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/personeller")
public class PersonelController {

    private final PersonelService personelService;

    public PersonelController(PersonelService personelService) {
        this.personelService = personelService;
    }

    @PostMapping
    public ResponseEntity<PersonelResponse> create(
            @Valid @RequestBody PersonelCreateRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(personelService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<PersonelResponse>> getAll() {
        return ResponseEntity.ok(personelService.getAll());
    }

    @GetMapping("/detayli")
    public ResponseEntity<List<PersonelVeArabalariResponse>> getAllWithArabalar() {
        return ResponseEntity.ok(personelService.getAllWithArabalar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PersonelResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(personelService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PersonelResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody PersonelUpdateRequest request) {

        return ResponseEntity.ok(personelService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        personelService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
