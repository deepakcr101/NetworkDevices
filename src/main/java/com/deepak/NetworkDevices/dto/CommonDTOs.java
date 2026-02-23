package com.deepak.NetworkDevices.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CommonDTOs {
    public record IdResponse(String id) {}

    public record ErrorResponse(
            String code,
            String message
    ) {}

    public static String nowReason(long ts) { return "ts=" + ts; }
}
