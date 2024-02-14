package kr.co.zeppy.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserRegisterByUsernameResponse {

    private String accessToken;
    private String refreshToken;
    private Long userId;
    private String username;
    private String nickname;
    private String userTag;
    private String imageUrl;
}