package kr.co.zeppy.user.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserRegisterByUsernameRequest {

    private String username;
    private String password;
}
