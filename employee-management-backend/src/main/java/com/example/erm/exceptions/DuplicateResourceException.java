package com.example.erm.exceptions;

public class DuplicateResourceException extends BaseException {
    public DuplicateResourceException(String message) {
        super(message, "DUPLICATE_RESOURCE");
    }
}