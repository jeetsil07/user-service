package com.ecommece.user_service.exception;

public class MissingFieldException extends RuntimeException{
    public MissingFieldException(String msg) {
        super(msg);
    }
}
