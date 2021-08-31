package com.intrasoft.skyroof.core.security.response;

import org.springframework.http.HttpStatus;

public class UnauthorizedResponseDTO {

    private final String status = HttpStatus.UNAUTHORIZED.toString();
    private final String message = "Please check your bearer token.";

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
