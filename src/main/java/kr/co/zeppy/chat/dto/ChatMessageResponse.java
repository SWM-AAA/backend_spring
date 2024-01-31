package kr.co.zeppy.chat.dto;

import kr.co.zeppy.chat.entity.MessageType;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessageResponse {
    private Long id;
    private Long chatRoomId;
    private Long userId;

    private String message;
    private MessageType messageType;
}