package kr.co.zeppy.global.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.zeppy.chat.dto.ChatMessageRequest;
import kr.co.zeppy.chat.dto.ChatMessageResponse;
import kr.co.zeppy.chat.entity.MessageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class RedisSubscriber implements MessageListener {
    private final ObjectMapper objectMapper;
    @Qualifier("redisChatTemplate")
    private final RedisTemplate<String, Object> redisTemplate;
    private final SimpMessageSendingOperations messagingTemplate;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String publishMessage = (String) redisTemplate.getStringSerializer().deserialize(message.getBody());

            ChatMessageRequest roomMessage = objectMapper.readValue(publishMessage, ChatMessageRequest.class);

            if (roomMessage.getMessageType().equals(MessageType.TALK)) {
                ChatMessageResponse chatMessageResponse = ChatMessageResponse.builder()
                        .chatRoomId(roomMessage.getId())
                        .userId(roomMessage.getUserId())
                        .message(roomMessage.getMessage())
                        .messageType(roomMessage.getMessageType())
                        .build();
                messagingTemplate.convertAndSend("/sub/chat/room/" + roomMessage.getId(), chatMessageResponse);
            }

        } catch (Exception e) {
            // todo : exception 만들어서 추가하기
//            throw new ChatMessageNotFoundException();
        }
    }
}