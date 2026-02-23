package com.deepak.NetworkDevices.dto.request;


import jakarta.validation.constraints.NotBlank;

public record CreateShelfRequest(
        @NotBlank(message = "shelfName is required") String shelfName,
        @NotBlank(message = "partName is required") String partName
) {}

