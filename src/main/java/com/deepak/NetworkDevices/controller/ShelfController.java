package com.deepak.NetworkDevices.controller;

import com.deepak.NetworkDevices.dto.request.*;
import com.deepak.NetworkDevices.dto.response.ShelfDto;
import com.deepak.NetworkDevices.dto.response.ShelfLiteDto;
import com.deepak.NetworkDevices.dto.response.ShelfWithStatusDto;
import com.deepak.NetworkDevices.service.ShelfService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/shelves")
public class ShelfController {

    private final ShelfService service;

    public ShelfController(ShelfService service) { this.service = service; }

    @GetMapping
    public ResponseEntity<List<ShelfDto>> get() {
        return ResponseEntity.ok(service.listShelvesWithStatus());
    }


    @PostMapping
    public ResponseEntity<ShelfDto> createShelf(@RequestBody CreateShelfRequest req) {
        String id = service.createShelf(req);
        ShelfDto dto = service.getShelf(id);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(dto);
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


    @GetMapping("/available")
    public ResponseEntity<List<ShelfLiteDto>> list() {
        return ResponseEntity.ok(service.listAvailableShelves());
    }
}