package kr.co.zeppy.global.redis.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BatteryRequest {

    private String battery;
    private Boolean isCharging;
}
