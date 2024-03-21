//package kr.co.zeppy.location.repository;
//
//import kr.co.zeppy.location.dto.FriendInfo;
//import kr.co.zeppy.location.entity.LocationMode;
//import kr.co.zeppy.user.entity.*;
//import kr.co.zeppy.user.repository.FriendshipRepository;
//import org.junit.jupiter.api.Disabled;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
//import org.springframework.test.context.ActiveProfiles;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@Disabled
//@ActiveProfiles("local")
//@DataJpaTest
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//public class LocationModeRepositoryTest {
//
//    @Autowired
//    private TestEntityManager entityManager;
//
//    @Autowired
//    private FriendshipRepository friendshipRepository;
//
//    @Autowired
//    private LocationModeRepository locationModeRepository;
//
//    @Test
//    @DisplayName("정확한 위치 친구 리스트 반환하기")
//    void findAccurateFriends() throws Exception {
//        // findAcceptedFriendsByUserId
//
//        User user0 = User.builder()
//                .nickname("User0")
//                .imageUrl("image0.jpg")
//                .userTag("User#0000")
//                .role(Role.USER)
//                .socialType(SocialType.KAKAO)
//                .socialId("12344")
//                .refreshToken("token0")
//                .build();
//
//        User user1 = User.builder()
//                .nickname("User1")
//                .imageUrl("image1.jpg")
//                .userTag("User#0001")
//                .role(Role.USER)
//                .socialType(SocialType.GOOGLE)
//                .socialId("54320")
//                .refreshToken("token1")
//                .build();
//
//        entityManager.persist(user0);
//        entityManager.persist(user1);
//
//        Friendship friendship = Friendship.builder()
//                .user(user0)
//                .friend(user1)
//                .status(FriendshipStatus.ACCEPTED)
//                .build();
//
//        LocationMode locationMode = LocationMode.builder()
//                .id()
//                .build();
//
//        entityManager.persist(friendship);
//        entityManager.flush();
//        entityManager.clear();
//
//        List<FriendInfo> accurateFriendList = locationModeRepository.findAccurateFriendsByUserId(user0.getId());
//
//        List<Long> result = new ArrayList<>();
//        result.add(user1.getId());
//
//        assertAll(
//                "start",
//                () -> assertEquals(acceptedUserId, result, "accepteduserid is not same result"),
//                () -> assertTrue(acceptedUserId.contains(user1.getId()))
//        );
//    }
//}
