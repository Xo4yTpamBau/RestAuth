package com.sprect.exception;

import org.springframework.security.core.AuthenticationException;

public class TryAuthException extends AuthenticationException {

    public TryAuthException(String msg, Throwable t) {
        super(msg, t);
    }

    public TryAuthException(String msg) {
        super(msg);
    }
}