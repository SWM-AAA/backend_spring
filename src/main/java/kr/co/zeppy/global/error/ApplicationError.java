package kr.co.zeppy.global.error;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ZeppyError {
    INVALID_JWT_TOKEN(HttpStatus.BAD_REQUEST, "COMMON_001", "token이 유효하지 않습니다."),
    EXPIRED_JWT_TOKEN(HttpStatus.BAD_REQUEST, "COMMON_002", "token 유효기간이 만료되었습니다."),

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_001", "서버 내부 에러가 발생하였습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    
}
