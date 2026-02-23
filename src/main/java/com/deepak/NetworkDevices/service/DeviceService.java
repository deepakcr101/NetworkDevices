package com.deepak.NetworkDevices.service;

import com.deepak.NetworkDevices.dto.DeviceDTOs.*;
import com.deepak.NetworkDevices.repo.DeviceRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.*;

@Service
public class DeviceService {

    private final DeviceRepository repo;
    private final Clock clock;

    public DeviceService(DeviceRepository repo, Clock clock) {
        this.repo = repo;
        this.clock = clock;
    }

    public String createDevice(CreateDeviceRequest req) {
        String deviceId = UUID.randomUUID().toString();
        long ts = now();

        // Pre-generate shelf positions (IDs + index)
        List<Map<String, Object>> positions = new ArrayList<>();
        for (int i = 1; i <= req.numberOfShelfPositions(); i++) {
            positions.add(Map.of("id", UUID.randomUUID().toString(), "idx", i));
        }
        return repo.createDeviceWithPositions(deviceId, req, positions, ts);
    }

    public List<DeviceResponse> listDevices(boolean includeDeleted) {
        return repo.listDevices(includeDeleted);
    }

    public DeviceSummaryResponse getDeviceSummary(String deviceId) {
        return repo.getDeviceSummary(deviceId);
    }

    public DeviceResponse updateDevice(String deviceId, UpdateDeviceRequest req) {
        Map<String, Object> updates = new HashMap<>();
        if (req.deviceName() != null) updates.put("deviceName", req.deviceName());
        if (req.partNumber() != null) updates.put("partNumber", req.partNumber());
        if (req.buildingName() != null) updates.put("buildingName", req.buildingName());
        if (req.deviceType() != null) updates.put("deviceType", req.deviceType());
        return repo.updateDevice(deviceId, updates, now());
    }

    public void deleteDevice(String deviceId) {
        repo.softDeleteDeviceCascade(deviceId, now());
    }

    private long now() {
        return Instant.now(clock).toEpochMilli();
    }
}
