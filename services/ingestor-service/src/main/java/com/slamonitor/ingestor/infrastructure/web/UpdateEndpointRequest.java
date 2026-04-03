package com.slamonitor.ingestor.infrastructure.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.util.Map;

public record UpdateEndpointRequest(
        @NotBlank String url,
        @Pattern(regexp = "GET|POST|PUT|DELETE|PATCH|HEAD|OPTIONS") String httpMethod,
        Map<String, String> headers,
        @NotNull @Positive Integer timeoutMs,
        @NotNull @Positive Integer intervalSecs
) {}
