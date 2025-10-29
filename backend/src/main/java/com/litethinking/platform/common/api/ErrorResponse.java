package com.litethinking.platform.common.api;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        List<String> details,
        String path
) {
    public static ErrorResponse of(int status, String error, String message, List<String> details, String path) {
        return new ErrorResponse(Instant.now(), status, error, message, details, path);
    }
}
