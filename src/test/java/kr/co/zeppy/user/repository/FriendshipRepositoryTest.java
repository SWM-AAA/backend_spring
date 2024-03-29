package kr.co.zeppy.user.repository;

import com.amazonaws.auth.profile.ProfilesConfigFile;
import kr.co.zeppy.user.entity.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ActiveProfiles;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("local")
@Disabled
@DataJpaTest(properties = "spring.profiles.active:local")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class FriendshipRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private FriendshipRepository friendshipRepository;

    @Test
    @DisplayName("유저 아이디로 수락된 친구 찾아서 반환하기")
    void findAcceptedFriendsByUserId() throws Exception {
        // findAcceptedFriendsByUserId

        User user0 = User.builder()
                .nickname("User0")
                .imageUrl("image0.jpg")
                .userTag("User#0000")
                .role(Role.USER)
                .socialType(SocialType.KAKAO)
                .socialId("12344")
                .refreshToken("token0")
                .build();

        User user1 = User.builder()
                .nickname("User1")
                .imageUrl("image1.jpg")
                .userTag("User#0001")
                .role(Role.USER)
                .socialType(SocialType.GOOGLE)
                .socialId("54320")
                .refreshToken("token1")
                .build();

        entityManager.persist(user0);
        entityManager.persist(user1);

        Friendship friendship = Friendship.builder()
                .user(user0)
                .friend(user1)
                .status(FriendshipStatus.ACCEPTED)
                .build();

        entityManager.persist(friendship);
        entityManager.flush();
        entityManager.clear();

        List<Long> acceptedUserId = friendshipRepository.findAcceptedFriendIdsByUserId(user0.getId());
        List<Long> result = new ArrayList<>();
        result.add(user1.getId());

        assertAll(
                "start",
                () -> assertEquals(acceptedUserId, result, "accepteduserid is not same result"),
                () -> assertTrue(acceptedUserId.contains(user1.getId()))
        );
    }
}