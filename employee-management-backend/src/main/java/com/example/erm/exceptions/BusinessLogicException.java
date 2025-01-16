package com.example.erm.exceptions;

public class BusinessLogicException extends BaseException {
    public BusinessLogicException(String message) {
        super(message, "BUSINESS_LOGIC_ERROR");
    }
}