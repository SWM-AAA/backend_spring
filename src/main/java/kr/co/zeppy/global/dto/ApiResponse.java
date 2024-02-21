package kr.co.zeppy.global.dto;

import kr.co.zeppy.global.error.ApplicationException;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApiResponse<T> {
    private boolean success;
    private T data;
    private ErrorResponse error;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> failure(ApplicationException ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), ex.getCode());
        return ApiResponse.<T>builder()
                .success(false)
                .error(errorResponse)
                .build();
    }
}
