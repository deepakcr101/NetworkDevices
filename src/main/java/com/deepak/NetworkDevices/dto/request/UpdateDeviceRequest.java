package com.deepak.NetworkDevices.dto.request;

public record UpdateDeviceRequest(
        String deviceName,
        String deviceType,
        String partNumber,
        String buildingName
) {}
