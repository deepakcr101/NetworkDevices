package com.deepak.NetworkDevices.dto.response;


import java.util.List;

public record DeviceSummaryResponse(
        DeviceDto device,
        List<ShelfPositionDto> positions
) {}

