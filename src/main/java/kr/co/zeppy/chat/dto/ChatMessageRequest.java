package kr.co.zeppy.chat.dto;

import kr.co.zeppy.chat.entity.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessageRequest {
    private Long id;
    private Long chatRoomId;
    private Long userId;

    private String message;
    private MessageType messageType;
}
