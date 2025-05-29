package org.olzhas.project.ExceptionHandler;

public class AuthException extends RuntimeException {
    public AuthException(String message) {
        super(message);
    }
}