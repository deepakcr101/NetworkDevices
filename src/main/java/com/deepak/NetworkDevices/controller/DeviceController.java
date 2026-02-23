package com.deepak.NetworkDevices.controller;

import com.deepak.NetworkDevices.dto.CommonDTOs.IdResponse;
import com.deepak.NetworkDevices.dto.DeviceDTOs.*;
import com.deepak.NetworkDevices.service.DeviceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    private final DeviceService service;

    public DeviceController(DeviceService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<DeviceResponse>> list(@RequestParam(defaultValue = "false") boolean includeDeleted) {
        return ResponseEntity.ok(service.listDevices(includeDeleted));
    }

    @PostMapping
    public ResponseEntity<IdResponse> create(@Valid @RequestBody CreateDeviceRequest req) {
        String id = service.createDevice(req);
        return ResponseEntity.created(URI.create("/api/devices/" + id)).body(new IdResponse(id));
    }

    @GetMapping("/{deviceId}")
    public ResponseEntity<DeviceSummaryResponse> get(@PathVariable String deviceId) {
        return ResponseEntity.ok(service.getDeviceSummary(deviceId));
    }

    @PatchMapping("/{deviceId}")
    public ResponseEntity<DeviceResponse> update(@PathVariable String deviceId,
                                                 @RequestBody UpdateDeviceRequest req) {
        return ResponseEntity.ok(service.updateDevice(deviceId, req));
    }

    @DeleteMapping("/{deviceId}")
    public ResponseEntity<Void> delete(@PathVariable String deviceId) {
        service.deleteDevice(deviceId);
        return ResponseEntity.noContent().build();
    }
}
