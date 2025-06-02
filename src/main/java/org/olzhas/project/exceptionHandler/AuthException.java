package org.olzhas.project.exceptionHandler;

public class AuthException extends RuntimeException {
    public AuthException(String message) {
        super(message);
    }
}