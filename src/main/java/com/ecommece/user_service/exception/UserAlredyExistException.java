package com.ecommece.user_service.exception;

public class UserAlredyExistException extends RuntimeException{
    public UserAlredyExistException(String msg) {
        super(msg);
    }
}
