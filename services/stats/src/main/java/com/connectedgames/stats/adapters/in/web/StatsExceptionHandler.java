package com.connectedgames.stats.adapters.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.connectedgames.stats.application.NotAllowedException;

@RestControllerAdvice
class StatsExceptionHandler {

    @ExceptionHandler(StatsNotFoundException.class)
    ProblemDetail notFound(StatsNotFoundException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(NotAllowedException.class)
    ProblemDetail forbidden(NotAllowedException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler(InvalidRequestException.class)
    ProblemDetail badRequest(InvalidRequestException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }
}
