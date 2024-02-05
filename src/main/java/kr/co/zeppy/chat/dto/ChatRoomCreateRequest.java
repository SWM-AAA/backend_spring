package kr.co.zeppy.chat.dto;

import lombok.*;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoomCreateRequest {
    // 참여할 사용자들의 id (본인 포함)
    private List<Long> userIdList;
}
