package kr.co.zeppy.location.dto;

import lombok.*;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UpdateLocationModeRequest {

    private List<Long> accurate;
    private List<Long> ambiguous;
    private List<Long> pinned;
}
