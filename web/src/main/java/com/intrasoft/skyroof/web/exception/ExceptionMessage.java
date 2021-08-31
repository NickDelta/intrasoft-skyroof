package com.intrasoft.skyroof.web.exception;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.springframework.web.server.ResponseStatusException;

@JsonPropertyOrder({"status", "reason"})
public class ExceptionMessage {

    private final ResponseStatusException exception;

    public ExceptionMessage(ResponseStatusException ex) {
        this.exception = ex;
    }

    @JsonProperty("status")
    public String getStatus() {
        return exception.getStatus().toString();
    }

    @JsonProperty("reason")
    public String getReason() {
        return exception.getReason();
    }
}