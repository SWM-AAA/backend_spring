package kr.co.zeppy.location.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LocationModeTimerResponse {

    private Integer shortest;
    private Integer secondShortest;
    private Integer medium;
    private Integer longest;
}
