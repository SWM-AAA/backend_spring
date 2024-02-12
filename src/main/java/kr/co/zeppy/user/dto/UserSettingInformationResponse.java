package kr.co.zeppy.user.dto;

import kr.co.zeppy.user.entity.SocialType;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSettingInformationResponse {

    private String nickname;
    private String userTag;
    private String imageUrl;
    private SocialType socialType;
}
