package com.cardanoj.exception;

public class AddressExcepion extends Exception {
    public AddressExcepion(String message) {
        super(message);
    }

    public AddressExcepion(String message, Exception e) {
        super(message, e);
    }
}
