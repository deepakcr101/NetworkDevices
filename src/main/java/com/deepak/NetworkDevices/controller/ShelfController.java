package com.deepak.NetworkDevices.controller;

import com.deepak.NetworkDevices.dto.request.*;
import com.deepak.NetworkDevices.dto.response.ShelfDto;
import com.deepak.NetworkDevices.dto.response.ShelfWithStatusDto;
import com.deepak.NetworkDevices.service.ShelfService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/shelves")
public class ShelfController {

    private final ShelfService service;

    public ShelfController(ShelfService service) { this.service = service; }

    @GetMapping("/{shelfId}")
    public ResponseEntity<ShelfDto> get(@PathVariable String shelfId) {
        return ResponseEntity.ok(service.getShelf(shelfId));
    }

    @PostMapping
    public ResponseEntity<String> create(@Valid @RequestBody CreateShelfRequest req) {
        var id = service.createShelf(req);
        return ResponseEntity.status(201).body(id);
    }

    @PatchMapping("/{shelfId}")
    public ResponseEntity<Void> update(@PathVariable String shelfId,
                                       @RequestBody UpdateShelfRequest req) {
        service.updateShelf(shelfId, req);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{shelfId}")
    public ResponseEntity<Void> delete(@PathVariable String shelfId) {
        service.softDeleteShelf(shelfId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/list-with-status")
    public ResponseEntity<List<ShelfWithStatusDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "false") boolean includeDeleted) {
        return ResponseEntity.ok(service.listShelvesWithStatus(page, size, includeDeleted));
    }
}