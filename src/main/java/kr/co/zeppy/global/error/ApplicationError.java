package kr.co.zeppy.global.error;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ApplicationError {
    // 401
    INVALID_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "COMMON_001", "token이 유효하지 않습니다 다시 로그인 해주세요."),
    EXPIRED_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "COMMON_002", "token 유효기간이 만료되었습니다 다시 로그인 해주세요."),

    // 404
    TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_003", "token을 찾을 수 없습니다."),

    // 500
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_001", "서버 내부 에러가 발생하였습니다."),

    // 503
    REDIS_SERVER_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "REDIS_001", "Redis 서버가 연결되지 않았습니다."),

    // 404
    USER_ID_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "사용자 ID를 찾을 수 없습니다."),
    USER_TAG_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_002", "사용자 User Tag를 찾을 수 없습니다."),
    USER_REFRESHTOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_003", "사용자 RefreshToken을 찾을 수 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_004", "사용자를 찾을 수 없습니다."),
    USER_NICKNAME_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_005", "사용자 닉네임을 찾을 수 없습니다."),
    // 400
    INVALID_USER_TAG_FORMAT(HttpStatus.BAD_REQUEST, "USER_006", "UserTag가 올바르지 않습니다."),

    FRIEND_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "FRIEND_001", "친구 요청을 찾을 수 없습니다."),

    FRIENDSHIP_NOT_FOUND(HttpStatus.NOT_FOUND, "FRIENDSHIP_001", "친구 요청을 찾을 수 없습니다."),
    FRIENDSHIP_ALREADY_ACCEPTED(HttpStatus.NOT_FOUND, "FRIENDSHIP_002", "이미 수락한 요청 입니다."),
    FRIENDSHIP_ALREADY_DECLINE(HttpStatus.NOT_FOUND, "FRIENDSHIP_003", "이미 거절한 요청 입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
