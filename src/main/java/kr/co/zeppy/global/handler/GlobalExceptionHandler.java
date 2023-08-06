package kr.co.zeppy.global.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import kr.co.zeppy.global.dto.ErrorResponse;
import kr.co.zeppy.global.error.ApplicationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorResponse> handleApplicationException(ApplicationException e) {
        log.error("handleApplicationException : ", e);
        ErrorResponse errorResponse = ErrorResponse.fromException(e);
        return new ResponseEntity<>(errorResponse, e.getStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unexpected error : ", e);
        ErrorResponse errorResponse = ErrorResponse.defaultError();
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
