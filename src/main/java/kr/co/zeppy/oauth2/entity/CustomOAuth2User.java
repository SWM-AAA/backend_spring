package kr.co.zeppy.oauth2.entity;

import kr.co.zeppy.user.entity.Role;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.util.Collection;
import java.util.Map;

@Getter
public class CustomOAuth2User extends DefaultOAuth2User {

    private String userTag;
    private Role role;


    public CustomOAuth2User(Collection<? extends GrantedAuthority> authorities,
                            Map<String, Object> attributes, String nameAttributeKey,
                            String userTag, Role role) {
        super(authorities, attributes, nameAttributeKey);
        this.userTag = userTag;
        this.role = role;
    }
}
