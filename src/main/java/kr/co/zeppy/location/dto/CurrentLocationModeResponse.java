package kr.co.zeppy.location.dto;

import lombok.*;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CurrentLocationModeResponse {

    private List<FriendInfo> accurate;
    private List<FriendInfo> ambiguous;
    private List<FriendInfo> pinned;
}
