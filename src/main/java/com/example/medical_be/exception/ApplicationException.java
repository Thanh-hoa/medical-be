package com.example.medical_be.exception;
import lombok.Getter;

@Getter
public class ApplicationException extends RuntimeException {
    public ApplicationException(String message){
        super(message);
    }
}
