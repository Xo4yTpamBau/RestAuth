package com.sprect.exception;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ResponseStatus;

public class StatusException extends AuthenticationException {

    public StatusException(String msg, Throwable t) {
        super(msg, t);
    }

    public StatusException(String msg) {
        super(msg);
    }
}
