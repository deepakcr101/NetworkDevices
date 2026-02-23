package com.deepak.NetworkDevices.controller;

import com.deepak.NetworkDevices.dto.request.*;
import com.deepak.NetworkDevices.dto.response.DeviceSummaryResponse;
import com.deepak.NetworkDevices.service.DeviceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/devices")
public class DeviceController {

    private final DeviceService service;

    public DeviceController(DeviceService service) { this.service = service; }

    @GetMapping("/{deviceId}/summary")
    public ResponseEntity<DeviceSummaryResponse> summary(@PathVariable String deviceId) {
        return ResponseEntity.ok(service.getDeviceSummary(deviceId));
    }

    @PostMapping
    public ResponseEntity<String> create(@Valid @RequestBody CreateDeviceRequest req) {
        var id = service.createDevice(req);
        return ResponseEntity.status(201).body(id);
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

    @PostMapping("/{deviceId}/positions/{shelfPositionId}:allocate")
    public ResponseEntity<Void> allocate(@PathVariable String deviceId,
                                         @PathVariable String shelfPositionId,
                                         @Valid @RequestBody AllocateRequest req) {
        service.allocate(deviceId, shelfPositionId, req);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{deviceId}/positions/{shelfPositionId}:free")
    public ResponseEntity<Void> free(@PathVariable String deviceId,
                                     @PathVariable String shelfPositionId) {
        service.free(deviceId, shelfPositionId);
        return ResponseEntity.ok().build();
    }
}
