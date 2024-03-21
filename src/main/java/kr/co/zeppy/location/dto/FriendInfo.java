package kr.co.zeppy.location.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FriendInfo {

    private Long userId;
    private String userTag;
    private String imageUrl;
}
