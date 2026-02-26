package com.deepak.NetworkDevices.dto.response;

import java.time.OffsetDateTime;

public record DeviceDto(
        String deviceId,
        String deviceName,
        String deviceType,
        String partNumber,
        String buildingName,
        int numberOfShelfPositions,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}

