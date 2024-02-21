package kr.co.zeppy.chat.service;

import jakarta.transaction.Transactional;
import kr.co.zeppy.chat.dto.ChatRoomResponse;
import kr.co.zeppy.chat.entity.ChatRoom;
import kr.co.zeppy.chat.repository.ChatRoomRepository;
import kr.co.zeppy.user.entity.User;
import kr.co.zeppy.user.entity.UserChatRoom;
import kr.co.zeppy.user.repository.UserChatRoomRepository;
import kr.co.zeppy.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final UserChatRoomRepository userChatRoomRepository;

    public ChatRoomResponse createChatRoom(List<Long> userIdList) {
        // 현재 시간 + 난수로 채팅방 이름 생성
        String roomName = generateRoomName();
        ChatRoom chatRoom = ChatRoom.builder()
                .roomName(roomName)
                .build();

        chatRoom = chatRoomRepository.save(chatRoom);

        // 사용자 ID 목록을 사용하여 사용자 객체 조회
        List<User> users = userRepository.findAllById(userIdList);

        for (User currentUser : users) {
            // 현재 사용자를 제외한 다른 사용자 이름으로 displayName 생성
            // ex) 루피, 조로, 상디 -> 루피 사용자에게는 채팅방 이름 (조로, 상디)
            // todo : 어떤 사용자가 이름 변경 시 사용자가 포함되어 있는 채팅방 전부 변경하는 로직 필요
            String displayName = users.stream()
                    .filter(user -> !user.equals(currentUser))
                    .map(User::getNickname)
                    .collect(Collectors.joining(", "));

            UserChatRoom userChatRoom = UserChatRoom.builder()
                    .user(currentUser)
                    .chatRoom(chatRoom)
                    .displayName(displayName)
                    .build();

            userChatRoomRepository.save(userChatRoom);
        }

        return ChatRoomResponse.builder()
                .id(chatRoom.getId())
                .name(chatRoom.getRoomName())
                .build();
    }

    public String generateRoomName() {
        // 현재 시간을 기반으로 하는 포맷 정의
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String timePart = dateFormat.format(new Date());

        SecureRandom secureRandom = new SecureRandom();
        int randomPart = secureRandom.nextInt(1000);

        // 시간 부분과 난수를 조합하여 채팅방 이름 생성 3자리로 format
        return "Room_" + timePart + "_" + String.format("%03d", randomPart);
    }
}
