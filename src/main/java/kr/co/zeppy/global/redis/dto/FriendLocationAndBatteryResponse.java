package kr.co.zeppy.global.redis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FriendLocationAndBatteryResponse {

    private List<FriendLocationAndBattery> accurate;
    private List<FriendLocationAndBattery> ambiguous;
    private List<FriendLocationAndBattery> pinned;
}
