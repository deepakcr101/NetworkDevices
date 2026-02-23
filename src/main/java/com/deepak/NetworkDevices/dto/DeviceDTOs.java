package com.deepak.NetworkDevices.dto;

import jakarta.validation.constraints.*;

import java.util.List;

public class DeviceDTOs {

    // Create
    public record CreateDeviceRequest(
            @NotBlank String deviceName,
            @NotBlank String partNumber,
            @NotBlank String buildingName,
            @NotBlank String deviceType,
            @Min(1) @Max(1000) int numberOfShelfPositions
    ) {}

    // Update (PATCH)
    public record UpdateDeviceRequest(
            String deviceName,
            String partNumber,
            String buildingName,
            String deviceType
            // Intentionally excluding numberOfShelfPositions in Phase 1
    ) {}

    // Basic response
    public record DeviceResponse(
            String deviceId,
            String deviceName,
            String partNumber,
            String buildingName,
            String deviceType,
            int numberOfShelfPositions,
            long createdAt,
            long updatedAt
    ) {}

    // ShelfPosition summary for device view
    public record ShelfPositionSummary(
            String shelfPositionId,
            int index,
            boolean occupied,
            String shelfId,
            String shelfName
    ) {}

    // Device Summary
    public record DeviceSummaryResponse(
            DeviceResponse device,
            List<ShelfPositionSummary> positions
    ) {}
}
