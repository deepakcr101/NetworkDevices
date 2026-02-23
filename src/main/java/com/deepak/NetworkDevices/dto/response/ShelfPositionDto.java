package com.deepak.NetworkDevices.dto.response;

public record ShelfPositionDto(
        String shelfPositionId,
        int index,
        boolean isOccupied,
        ShelfLiteDto shelf  // nullable
) {}


