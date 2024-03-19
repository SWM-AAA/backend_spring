package kr.co.zeppy.location.dto;

import kr.co.zeppy.user.entity.User;
import lombok.*;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UpdateLocationModeRequest {

    private List<User> accurateFriends;
    private List<User> ambiguousFriends;
    private List<User> pinnedFriends;
}
