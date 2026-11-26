package com.flota.camion.exception;

public class PatenteDuplicadaException extends RuntimeException {
    public PatenteDuplicadaException() {
        super();
    }

    public PatenteDuplicadaException(String message) {
        super(message);
    }

    public PatenteDuplicadaException(String message, Exception e) {
        super(message, e);
    }
}
