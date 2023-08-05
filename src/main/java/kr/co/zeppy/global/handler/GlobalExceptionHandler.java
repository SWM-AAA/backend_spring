package kr.co.zeppy.global.handler;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import kr.co.zeppy.global.dto.ErrorResponse;
import kr.co.zeppy.global.error.ZeppyException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ZeppyException.class)
    public ResponseEntity<ErrorResponse> handleZeppyException(ZeppyException e) {
        log.error("handleZeppyException", e);
        ErrorResponse errorResponse = ErrorResponse.of(e);
        return new ResponseEntity<>(errorResponse, e.getStatus());
    }
}
