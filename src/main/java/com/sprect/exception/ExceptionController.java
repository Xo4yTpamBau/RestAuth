package com.sprect.exception;

import com.sprect.model.response.ResponseError;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.oxm.ValidationFailureException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDate;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class ExceptionController extends ResponseEntityExceptionHandler {

    @ExceptionHandler(UsernameNotFoundException.class)
    protected ResponseEntity<Object> userNotFound(Exception ex) {
        return new ResponseEntity<>(new ResponseError(
                new Date().toString(),
                404, ex.getMessage()),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({JwtException.class, ExpiredJwtException.class})
    protected ResponseEntity<Object> jwt(Exception ex) {
        return new ResponseEntity<>(new ResponseError(
                new Date().toString(),
                401, ex.getMessage()),
                HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ValidationFailureException.class)
    protected ResponseEntity<Object> validator(Exception ex) {
        return new ResponseEntity<>(new ResponseError(
                new Date().toString(),
                400, ex.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(StatusException.class)
    protected ResponseEntity<Object> checkStatus(Exception ex) {
        return new ResponseEntity<>(new ResponseError(
                new Date().toString(),
                403, ex.getMessage()),
                HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(TryAuthException.class)
    protected ResponseEntity<Object> tryAuth(Exception ex) {
        return new ResponseEntity<>(new ResponseError(
                new Date().toString(),
                403, ex.getMessage()),
                HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(RegistrationException.class)
    protected ResponseEntity<Object> registration(Exception ex) {
        return new ResponseEntity<>(new ResponseError(
                new Date().toString(),
                400, ex.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

//    @ExceptionHandler(RuntimeException.class)
//    protected ResponseEntity<Object> runtime(Exception ex){
//        return new ResponseEntity<>(new ResponseError(
//                new Date().toString(),
//                500, ex.getMessage()),
//                HttpStatus.INTERNAL_SERVER_ERROR);
//    }

//    @ExceptionHandler({RuntimeException.class})
//    protected ResponseEntity<Object> runtime(Exception ex){
//        Map<String, Object> newBody = new LinkedHashMap<>();
//
//        newBody.put("timestamp", LocalDate.now());
//        newBody.put("status", 500);
//        newBody.put("errors", ex.getMessage());
//
//        return new ResponseEntity<>(newBody, HttpStatus.INTERNAL_SERVER_ERROR);
//    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatus status,
                                                                  WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDate.now());
        body.put("status", status.value());

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());

        body.put("error", errors);

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
}
