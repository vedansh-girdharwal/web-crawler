package com.ecommerce.webcrawler.handlers;

import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Global exception handler for the application that catches and processes exceptions
 * across all controllers
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Error response structure containing error details
     */
    @Data
    public static class ErrorResponse {
        // Human-readable error message
        private final String message;
        // Technical error details or exception message
        private final String error;
    }

    /**
     * Handles all uncaught exceptions in the application
     * 
     * @param e The exception that was thrown
     * @return ResponseEntity containing error details with HTTP 400 status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        ErrorResponse error = new ErrorResponse(
            "An error occurred while processing the request",
            e.getMessage()
        );
        return ResponseEntity.badRequest().body(error);
    }
}