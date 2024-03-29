package com.cardanoj.crypto.exception;

public class KeyException extends RuntimeException {

    public KeyException(String message) {
        super(message);
    }

    public KeyException(String message, Exception e) {
        super(message, e);
    }
}
