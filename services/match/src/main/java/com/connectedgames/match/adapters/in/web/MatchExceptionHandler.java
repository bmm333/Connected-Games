package com.connectedgames.match.adapters.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.connectedgames.match.application.MatchNotFoundException;

@RestControllerAdvice
class MatchExceptionHandler{
    @ExceptionHandler(MatchNotFoundException.class)
    ProblemDetail notFound(MatchNotFoundException e){
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,e.getMessage());
    }
}
