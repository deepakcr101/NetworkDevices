package com.deepak.NetworkDevices.dto.request;


import jakarta.validation.constraints.NotBlank;

public record AllocateRequest(
        @NotBlank(message = "shelfId is required") String shelfId
) {}

