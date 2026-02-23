package com.deepak.NetworkDevices.dto.response;


import java.time.OffsetDateTime;

public record ShelfDto(
        String shelfId,
        String shelfName,
        String partName,
        boolean isDeleted,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}

