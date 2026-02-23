package com.deepak.NetworkDevices.dto.error;

import java.time.OffsetDateTime;

public record ApiError(
        int status,
        String error,
        String code,
        String message,
        String path,
        String traceId,
        OffsetDateTime timestamp
) {}

