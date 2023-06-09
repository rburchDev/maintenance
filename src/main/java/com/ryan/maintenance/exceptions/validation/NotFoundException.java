package com.ryan.maintenance.exceptions.validation;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class NotFoundException extends Exception{
    public NotFoundException(String message) {super(message);}
    public NotFoundException(Throwable cause) {initCause(cause);}

    public NotFoundException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }
}
