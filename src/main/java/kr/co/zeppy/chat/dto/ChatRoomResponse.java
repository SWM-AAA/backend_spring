package kr.co.zeppy.chat.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoomResponse {
    private Long id;
    private String name;
}
