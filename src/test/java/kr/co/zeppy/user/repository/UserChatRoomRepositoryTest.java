package kr.co.zeppy.user.repository;

import kr.co.zeppy.chat.entity.ChatRoom;
import kr.co.zeppy.chat.entity.ReadStatus;
import kr.co.zeppy.user.entity.Role;
import kr.co.zeppy.user.entity.SocialType;
import kr.co.zeppy.user.entity.User;
import kr.co.zeppy.user.entity.UserChatRoom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

@ActiveProfiles("local")
@Disabled
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserChatRoomRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserChatRoomRepository userChatRoomRepository;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("유저 아이디로 유저가 속한 채팅방 인스턴스 리스트로 반환하기")
    void findByUserId() throws Exception {
        // User 엔티티 생성 및 영속화
        User user = User.builder()
                .nickname("User0")
                .imageUrl("image0.jpg")
                .userTag("User#0000")
                .role(Role.USER)
                .socialType(SocialType.KAKAO)
                .socialId("12344")
                .refreshToken("token0")
                .activated(true)
                .build();
        entityManager.persist(user);

        // ChatRoom 엔티티 생성 및 영속화
        ChatRoom chatRoom = ChatRoom.builder()
                .roomName("chatRoom0")
                .build();
        entityManager.persist(chatRoom);

        // UserChatRoom 엔티티 생성 및 영속화
        UserChatRoom userChatRoom = UserChatRoom.builder()
                .user(user)
                .chatRoom(chatRoom)
                .build();
        entityManager.persist(userChatRoom);

        // 변경 사항을 데이터베이스에 반영
        entityManager.flush();

        entityManager.clear();

        List<UserChatRoom> userChatRooms = userChatRoomRepository.findByUserId(user.getId());

        assertAll(
                "UserChatRoom findByUserId test",
                () -> assertThat(userChatRooms).isNotEmpty(),
                () -> assertThat(userChatRooms.size()).isEqualTo(1),
                () -> {
                    UserChatRoom firstUserChatRoom = userChatRooms.get(0);
                    assertAll(
                            () -> assertThat(firstUserChatRoom.getUser().getNickname()).isEqualTo("User0"),
                            () -> assertThat(firstUserChatRoom.getChatRoom().getRoomName()).isEqualTo("chatRoom0")
                    );
                }
        );
    }
}