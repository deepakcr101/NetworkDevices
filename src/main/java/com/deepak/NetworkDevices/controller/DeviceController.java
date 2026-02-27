package com.deepak.NetworkDevices.controller;

import com.deepak.NetworkDevices.dto.request.*;
import com.deepak.NetworkDevices.dto.response.DeviceDto;
import com.deepak.NetworkDevices.dto.response.DeviceSummaryResponse;
import com.deepak.NetworkDevices.service.DeviceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/devices")
public class DeviceController {

    private final DeviceService service;

    public DeviceController(DeviceService service) { this.service = service; }

    @GetMapping("/all")
    public ResponseEntity<List<DeviceDto>> getAllDevices() {
        // 1. If service.getDevices() returns an empty list,
        //    this returns 200 OK with [] (Correct REST behavior).
        // 2. Passing null to .ok() still returns 200, so ensure your service
        //    returns Collections.emptyList() instead of null.
        return ResponseEntity.ok(service.getDevices());
    }

    @GetMapping("/{deviceId}/summary")
    public ResponseEntity<DeviceSummaryResponse> getSummary(@PathVariable String deviceId) {
        return ResponseEntity.ok(service.getDeviceSummary(deviceId));
    }

    @PostMapping
    public ResponseEntity<String> create(@Valid @RequestBody CreateDeviceRequest req) {
        String id = service.createDevice(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    @PatchMapping("/{deviceId}")
    public ResponseEntity<Void> update(@PathVariable String deviceId,
                                       @RequestBody UpdateDeviceRequest req) {
        service.updateDevice(deviceId, req);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{deviceId}")
    public ResponseEntity<Void> delete(@PathVariable String deviceId) {
        service.softDeleteDevice(deviceId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{deviceId}/shelf-positions/{shelfPositionId}:allocate")
    public ResponseEntity<Void> allocate(@PathVariable String deviceId,
                                         @PathVariable String shelfPositionId,
                                         @Valid @RequestBody AllocateRequest req) {
        service.allocate(deviceId, shelfPositionId, req);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{deviceId}/shelf-positions/{shelfPositionId}:free")
    public ResponseEntity<Void> free(@PathVariable String deviceId,
                                     @PathVariable String shelfPositionId) {
        service.free(deviceId, shelfPositionId);
        return ResponseEntity.ok().build();
    }
}
