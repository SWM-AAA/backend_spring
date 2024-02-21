package kr.co.zeppy.chat.service;

import jakarta.transaction.Transactional;
import kr.co.zeppy.chat.dto.ChatMessageRequest;
import kr.co.zeppy.chat.entity.ChatMessage;
import kr.co.zeppy.chat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepositor;

//    public ChatMessage saveChatMessage(ChatMessageRequest chatMessageRequest) {
//        ChatMessage chatMessage = ChatMessage.builder()
//                .chatRoomId(chatMessageRequest.getChatRoomId())
//                .userId(chatMessageRequest.getUserId())
//                .
//    }
}
