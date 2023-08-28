package kr.co.zeppy.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import kr.co.zeppy.user.entity.User;
import lombok.AccessLevel;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
// 누가 친구 요청을 했는지 사용되는 User info
public class UserFriendInfoResponse {
    private Long userId;
    private String nickname;
    private String imageUrl;
    private String userTag;

    public static UserFriendInfoResponse from(User user) {
        return new UserFriendInfoResponse(
            user.getId(),
            user.getNickname(),
            user.getImageUrl(),
            user.getUserTag()
        );
    }
}
