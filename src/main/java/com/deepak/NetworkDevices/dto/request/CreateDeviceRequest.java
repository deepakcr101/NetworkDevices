package com.deepak.NetworkDevices.dto.request;

import jakarta.validation.constraints.*;

public record CreateDeviceRequest(
        @NotBlank(message = "deviceName is required") String deviceName,
        @NotBlank(message = "deviceType is required") String deviceType,
        @NotBlank(message = "partNumber is required") String partNumber,
        @NotBlank(message = "buildingName is required") String buildingName,
        @Min(value = 1, message = "numberOfShelfPositions must be >= 1") int numberOfShelfPositions
) {}
