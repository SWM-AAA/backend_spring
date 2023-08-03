package kr.co.zeppy.global.redis.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LocationAndBatteryRequest {

    private String latitude;
    private String longitude;
    private String battery;
    private Boolean isCharging;
}
