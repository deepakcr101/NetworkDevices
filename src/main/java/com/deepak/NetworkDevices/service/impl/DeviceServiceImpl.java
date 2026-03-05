package com.deepak.NetworkDevices.service.impl;

import com.deepak.NetworkDevices.dto.request.*;
import com.deepak.NetworkDevices.dto.response.*;
import com.deepak.NetworkDevices.exception.*;
import com.deepak.NetworkDevices.repo.DeviceRepository;
import com.deepak.NetworkDevices.service.DeviceService;
import org.neo4j.driver.Record;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository repo;
    private final String database;

    public DeviceServiceImpl(DeviceRepository repo,
                             @Value("${neo4j.database:neo4j}") String database) {
        this.repo = repo;
        this.database = database;
    }

    @Override
    public List<DeviceDto> getDevices() {
        return repo.getDevices();
    }

    @Override
    public String createDevice(CreateDeviceRequest req) {
        Map<String,Object> params = new HashMap<>();
        params.put("deviceName", req.deviceName());
        params.put("deviceType", req.deviceType());
        params.put("partNumber", req.partNumber());
        params.put("buildingName", req.buildingName());
        params.put("numSP", req.numberOfShelfPositions());
        params.put("database", database);
        return repo.createDevice(params).asString();

    }

    @Override
    public DeviceSummaryResponse getDeviceSummary(String deviceId) {
        var rec = repo.getDeviceSummary(deviceId)
                .orElseThrow(() -> new NotFoundException("Device not found: " + deviceId));
        var d = rec.get("d").asNode();
        var positions = rec.get("positions").asList(v -> {
            var m = v.asMap();
            var shelfMap = (Map<String,Object>) m.get("shelf");
            ShelfLiteDto shelf = (shelfMap == null) ? null :
                    new ShelfLiteDto(
                            shelfMap.get("shelfId").toString(),
                            shelfMap.get("shelfName").toString(),
                            shelfMap.get("partName").toString());
            return new ShelfPositionDto(
                    Objects.toString(m.get("shelfPositionId"), null),
                    ((Number)m.get("index")).intValue(),
                    (Boolean)m.get("isOccupied"),
                    shelf
            );
        });
        var device = new DeviceDto(
                d.get("deviceId").asString(),
                d.get("deviceName").asString(),
                d.get("deviceType").asString(),
                d.get("partNumber").asString(),
                d.get("buildingName").asString(),
                d.get("numberOfShelfPositions").asInt(),
                null, null
        );
        return new DeviceSummaryResponse(device, positions);
    }

    @Override
    public void updateDevice(String deviceId, UpdateDeviceRequest req) {
        Map<String,Object> updates = new HashMap<>();
        if (req.deviceName() != null) updates.put("deviceName", req.deviceName());
        if (req.deviceType() != null) updates.put("deviceType", req.deviceType());
        if (req.partNumber() != null) updates.put("partNumber", req.partNumber());
        if (req.buildingName() != null) updates.put("buildingName", req.buildingName());

        if (updates.isEmpty()) return;

        boolean ok = repo.updateDevice(deviceId, updates, database);
        if (!ok) throw new NotFoundException("Device not found or deleted: " + deviceId);
    }

    @Override
    public void softDeleteDevice(String deviceId) {
        boolean ok = repo.softDeleteDevice(deviceId, database);
        if (!ok) throw new NotFoundException("Device not found or already deleted: " + deviceId);
    }

    @Override
    public void allocate(String deviceId, String shelfPositionId, AllocateRequest req) {
        var rec = repo.allocate(deviceId, shelfPositionId, req.shelfId(), database)
                .orElseThrow(() -> new ConflictException("Allocation failed (occupied or shelf already linked)"));
    }

    @Override
    public void free(String deviceId, String shelfPositionId) {
        boolean ok = repo.free(deviceId, shelfPositionId, database);
        if (!ok) throw new NotFoundException("Position not allocated or not found");
    }
}