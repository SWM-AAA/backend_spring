package kr.co.zeppy.global.redis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FriendLocationAndBattery {
    private String latitude;
    private String longitude;
    private String battery;
    private Boolean isCharging;
}
