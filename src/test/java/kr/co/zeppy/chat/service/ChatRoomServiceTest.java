package kr.co.zeppy.chat.service;

import kr.co.zeppy.chat.dto.ChatRoomResponse;
import kr.co.zeppy.chat.entity.ChatRoom;
import kr.co.zeppy.chat.repository.ChatRoomRepository;
import kr.co.zeppy.user.entity.User;
import kr.co.zeppy.user.repository.UserChatRoomRepository;
import kr.co.zeppy.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
//@SpringBootTest
class ChatRoomServiceTest {
    @Mock
    private ChatRoomRepository chatRoomRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserChatRoomRepository userChatRoomRepository;

    @InjectMocks
    private ChatRoomService chatRoomService;

    @BeforeEach
    void setUp() {
//        MockitoAnnotations.initMocks(this);
    }

    @Test
    void createChatRoom() {
        // Given
        Long userId1 = 1L;
        Long userId2 = 2L;
        List<Long> userIdList = Arrays.asList(userId1, userId2);

        User user1 = User.builder().id(userId1).nickname("Nickname1").build();
        User user2 = User.builder().id(userId2).nickname("Nickname2").build();

        List<User> users = Arrays.asList(user1, user2);

        ChatRoom chatRoom = ChatRoom.builder().id(1L).roomName("TestRoom").build();

        // When
        when(userRepository.findAllById(userIdList)).thenReturn(users);
        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(chatRoom);

        ChatRoomResponse response = chatRoomService.createChatRoom(userIdList);

        // Then
        assertNotNull(response);
        assertEquals(chatRoom.getId(), response.getId());

    }
}