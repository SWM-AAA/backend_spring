package kr.co.zeppy.oauth2.entity;

import kr.co.zeppy.oauth2.userinfo.GoogleOAuth2UserInfo;
import kr.co.zeppy.oauth2.userinfo.KakaoOAuth2UserInfo;
import kr.co.zeppy.oauth2.userinfo.OAuth2UserInfo;
import kr.co.zeppy.user.entity.Role;
import kr.co.zeppy.user.entity.SocialType;
import kr.co.zeppy.user.entity.User;
import kr.co.zeppy.user.service.NickNameService;
import lombok.Builder;
import lombok.Getter;
import java.util.Map;


// OAuth2 로그인을 위한 OAuthAttributes 클래스
@Getter
public class OAuthAttributes {

    private String nameAttributeKey; // OAuth2 로그인 진행 시 키가 되는 필드 값, PK와 같은 의미
    private OAuth2UserInfo oauth2UserInfo; // 소셜 타입별 로그인 유저 정보(닉네임, 이메일, 프로필 사진 등등)

    @Builder
    public OAuthAttributes(String nameAttributeKey, OAuth2UserInfo oauth2UserInfo) {
        this.nameAttributeKey = nameAttributeKey;
        this.oauth2UserInfo = oauth2UserInfo;
    }

    private NickNameService nickNameService;


    public static OAuthAttributes of(SocialType socialType,
                                     String userNameAttributeName, Map<String, Object> attributes) {

        if (socialType == SocialType.KAKAO) {
            return ofKakao(userNameAttributeName, attributes);
        }
        return ofGoogle(userNameAttributeName, attributes);
    }


    private static OAuthAttributes ofKakao(String userNameAttributeName, Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .nameAttributeKey(userNameAttributeName)
                .oauth2UserInfo(new KakaoOAuth2UserInfo(attributes))
                .build();
    }

    
    public static OAuthAttributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .nameAttributeKey(userNameAttributeName)
                .oauth2UserInfo(new GoogleOAuth2UserInfo(attributes))
                .build();
    }


    public User toEntity(SocialType socialType, OAuth2UserInfo oauth2UserInfo, String userTag) {
        // usertag 변경 로직 추가
        
        return User.builder()
                .socialType(socialType)
                .socialId(oauth2UserInfo.getId())
                .userTag(userTag)
                .nickname(oauth2UserInfo.getNickname())
                .imageUrl(oauth2UserInfo.getImageUrl())
                .role(Role.GUEST)
                .activated(true)
                .build();
    }
}
