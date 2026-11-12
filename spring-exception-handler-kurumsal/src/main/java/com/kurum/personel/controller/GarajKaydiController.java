package com.kurum.personel.controller;

import com.kurum.personel.dto.request.GarajKaydiRequest;
import com.kurum.personel.dto.response.GarajKaydiResponse;
import com.kurum.personel.service.GarajKaydiService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/garaj-kayitlari")
public class GarajKaydiController {

    private final GarajKaydiService garajKaydiService;

    public GarajKaydiController(GarajKaydiService garajKaydiService) {
        this.garajKaydiService = garajKaydiService;
    }

    @PostMapping
    public ResponseEntity<GarajKaydiResponse> create(@Valid @RequestBody GarajKaydiRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(garajKaydiService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<GarajKaydiResponse>> getAll() {
        return ResponseEntity.ok(garajKaydiService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GarajKaydiResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(garajKaydiService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GarajKaydiResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody GarajKaydiRequest request) {
        return ResponseEntity.ok(garajKaydiService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        garajKaydiService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
