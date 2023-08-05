package kr.co.zeppy.global.dto;

import kr.co.zeppy.global.error.ZeppyException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ErrorResponse {
    private final String message;
    private final String code;

    public static ErrorResponse of(ZeppyException zeppyException) {
        return new ErrorResponse(zeppyException.getMessage(), zeppyException.getCode());
    }
}
