package com.example.template.common.exception;

/**
 * Thrown when a change would violate a business uniqueness or state rule. Mapped to HTTP 409.
 */
public class ResourceConflictException extends RuntimeException {

    public ResourceConflictException(String message) {
        super(message);
    }
}
