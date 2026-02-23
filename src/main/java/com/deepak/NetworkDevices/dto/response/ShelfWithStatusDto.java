package com.deepak.NetworkDevices.dto.response;


public record ShelfWithStatusDto(
        ShelfDto shelf,
        String status  // "Unallocated" or "DeviceName:PosIndex"
) {}

