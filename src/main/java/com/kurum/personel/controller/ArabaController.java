package com.kurum.personel.controller;

import com.kurum.personel.dto.request.ArabaCreateRequest;
import com.kurum.personel.dto.request.ArabaUpdateRequest;
import com.kurum.personel.dto.response.ArabaResponse;
import com.kurum.personel.service.ArabaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/arabalar")
public class ArabaController {

    private final ArabaService arabaService;

    public ArabaController(ArabaService arabaService) {
        this.arabaService = arabaService;
    }

    @PostMapping
    public ResponseEntity<ArabaResponse> create(@Valid @RequestBody ArabaCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(arabaService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<ArabaResponse>> getAll() {
        return ResponseEntity.ok(arabaService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArabaResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(arabaService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ArabaResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ArabaUpdateRequest request) {
        return ResponseEntity.ok(arabaService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        arabaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
