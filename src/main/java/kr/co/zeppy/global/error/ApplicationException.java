package kr.co.zeppy.global.error;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class ZeppyException extends RuntimeException {
    private final HttpStatus status;
    private final String code;

    public ZeppyException(ZeppyError error) {
        super(error.getMessage());
        this.status = error.getStatus();
        this.code = error.getCode();
    }
}
