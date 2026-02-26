package com.deepak.NetworkDevices.dto.response;


import java.time.OffsetDateTime;

public record ShelfDto(
        String shelfId,
        String shelfName,
        String partName,
        String deviceId,
        String shelfPositionId,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}

