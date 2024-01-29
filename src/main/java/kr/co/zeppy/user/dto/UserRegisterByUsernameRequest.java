package kr.co.zeppy.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class UserRegisterByUsernameRequest {

    private String username;
    private String password;
    private String nickname;
}
