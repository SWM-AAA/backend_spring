package kr.co.zeppy.user.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import kr.co.zeppy.global.jwt.service.JwtService;
import kr.co.zeppy.user.dto.ConfirmFriendshipRequest;
import kr.co.zeppy.user.dto.FriendshipRequest;
import kr.co.zeppy.user.dto.UserFriendInfoResponse;
import kr.co.zeppy.user.entity.Friendship;
import kr.co.zeppy.user.entity.FriendshipStatus;
import kr.co.zeppy.user.entity.NicknameCounter;
import kr.co.zeppy.user.entity.Role;
import kr.co.zeppy.user.entity.SocialType;
import kr.co.zeppy.user.entity.User;
import kr.co.zeppy.user.repository.FriendshipRepository;
import kr.co.zeppy.user.repository.NickNameRepository;
import kr.co.zeppy.user.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito; 

@SpringBootTest
public class FriendServiceTest {

    @Autowired
    private FriendService friendService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private FriendshipRepository friendshipRepository;

    @MockBean
    private UserRepository userRepository;

    private static final String TOKEN = "sample_token";
    private static final Long INIT_USERID = 1L;
    private static final Long INIT_FRIENDID = 2L;
    private static final Long INIT_FRIENDSHIPID = 1L;
    private static final String USER_NICKNAME = "UserNickname";
    private static final String USER_IMAGE_URL = "userImageUrl";
    private static final String USER_TAG = "User#0001";
    private static final Role USER_ROLE = Role.USER;
    private static final SocialType USER_SOCIAL_TYPE = SocialType.KAKAO;
    private static final String USER_SOCIAL_ID = "userSocialId";
    private static final String USER_REFRESH_TOKEN = "userRefreshToken";
    private static final String FRIEND_NICKNAME = "FriendNickname";
    private static final String FRIEND_IMAGE_URL = "friendImageUrl";
    private static final String FRIEND_TAG = "Friend#0001";
    private static final Role FRIEND_ROLE = Role.USER;
    private static final SocialType FRIEND_SOCIAL_TYPE = SocialType.GOOGLE;
    private static final String FRIEND_SOCIAL_ID = "friendSocialId";
    private static final String FRIEND_REFRESH_TOKEN = "friendRefreshToken";

    private FriendshipRequest friendshipRequest;
    private User user;
    private User friend;

    @BeforeEach
    public void setup() {
        user = User.builder()
                .id(INIT_USERID)
                .nickname(USER_NICKNAME)
                .imageUrl(USER_IMAGE_URL)
                .userTag(USER_TAG)
                .role(USER_ROLE)
                .socialType(USER_SOCIAL_TYPE)
                .socialId(USER_SOCIAL_ID)
                .refreshToken(USER_REFRESH_TOKEN)
                .build();
                
        friend = User.builder()
                .id(INIT_FRIENDID)
                .nickname(FRIEND_NICKNAME)
                .imageUrl(FRIEND_IMAGE_URL)
                .userTag(FRIEND_TAG)
                .role(FRIEND_ROLE)
                .socialType(FRIEND_SOCIAL_TYPE)
                .socialId(FRIEND_SOCIAL_ID)
                .refreshToken(FRIEND_REFRESH_TOKEN)
                .build();

        friendshipRequest = FriendshipRequest.builder()
                .userId(INIT_FRIENDID)
                .build();
        when(jwtService.getLongUserIdFromToken(anyString())).thenReturn(INIT_USERID);
        when(userRepository.findById(INIT_USERID)).thenReturn(Optional.of(user));
        when(userRepository.findById(INIT_FRIENDID)).thenReturn(Optional.of(friend));
    }

    @Test
    void test_Send_Friend_Request() {
        // when
        friendService.sendFriendRequest(TOKEN, friendshipRequest);
        
        // then
        verify(friendshipRepository, times(1)).save(any(Friendship.class));
        
        Friendship createdUserFriendship = user.getSentFriendships().iterator().next();
        Friendship createdFriendFriendship = friend.getReceivedFriendships().iterator().next();
    
        assertAll(
            () -> assertEquals(INIT_USERID, createdUserFriendship.getUser().getId()),
            () -> assertEquals(INIT_FRIENDID, createdUserFriendship.getFriend().getId()),
            () -> assertEquals(FriendshipStatus.PENDING, createdUserFriendship.getStatus()),
    
            () -> assertEquals(INIT_FRIENDID, createdFriendFriendship.getFriend().getId()),
            () -> assertEquals(INIT_USERID, createdFriendFriendship.getUser().getId()),
            () -> assertEquals(FriendshipStatus.PENDING, createdFriendFriendship.getStatus())
        );
    }
    

    
    @Test
    void test_Check_Friend_Request_To_List() {
        // given
        Friendship receivedFriendship = Friendship.builder()
                .user(friend) // 요청을 보낸 사용자
                .friend(user) // 요청을 받은 사용자 (자신)
                .status(FriendshipStatus.PENDING)
                .build();

        user.getReceivedFriendships().add(receivedFriendship);

        when(userRepository.findById(INIT_USERID)).thenReturn(Optional.of(user));

        // when
        List<UserFriendInfoResponse> result = friendService.checkFriendRequestToList(INIT_USERID);

        // then
        assertEquals(1, result.size());
        assertEquals(FRIEND_NICKNAME, result.get(0).getNickname());
    }

    @Test
    void test_Check_Sent_Friend_Request_To_List() {
        // given
        Friendship sentFriendship = Friendship.builder()
                .user(user) // 요청을 보낸 사용자 (자신)
                .friend(friend) // 요청을 받은 사용자
                .status(FriendshipStatus.PENDING)
                .build();

        user.getSentFriendships().add(sentFriendship);

        when(userRepository.findById(INIT_USERID)).thenReturn(Optional.of(user));

        // when
        List<UserFriendInfoResponse> result = friendService.checkSentFriendRequestToList(INIT_USERID);

        // then
        assertEquals(1, result.size());
        assertEquals(FRIEND_NICKNAME, result.get(0).getNickname());
    }



    @Test
    void test_Confirm_Friendship_Accept() {
        // given
        ConfirmFriendshipRequest request = ConfirmFriendshipRequest.builder()
                .userId(INIT_FRIENDID)
                .isAccept(true)
                .build();
        Friendship pendingFriendship = Friendship.builder()
                .user(friend)
                .friend(user)
                .status(FriendshipStatus.PENDING)
                .build();
        when(friendshipRepository.findByUserIdAndFriendId(INIT_FRIENDID, INIT_USERID))
                .thenReturn(Optional.of(pendingFriendship));

        // when
        friendService.confirmFriendship(INIT_USERID, request);

        // then
        assertEquals(FriendshipStatus.ACCEPTED, pendingFriendship.getStatus());
        assertFalse(friend.getSentFriendships().contains(pendingFriendship));
        assertFalse(user.getReceivedFriendships().contains(pendingFriendship));
    }

    @Test
    void test_Confirm_Friendship_Decline() {
        // given
        ConfirmFriendshipRequest request = ConfirmFriendshipRequest.builder()
                .userId(INIT_FRIENDID)
                .isAccept(false)
                .build();
        Friendship pendingFriendship = Friendship.builder()
                .user(friend)
                .friend(user)
                .status(FriendshipStatus.PENDING)
                .build();
        when(friendshipRepository.findByUserIdAndFriendId(INIT_FRIENDID, INIT_USERID))
                .thenReturn(Optional.of(pendingFriendship));

        // when
        friendService.confirmFriendship(INIT_USERID, request);

        // then
        assertEquals(FriendshipStatus.DECLINE, pendingFriendship.getStatus());
        assertFalse(friend.getSentFriendships().contains(pendingFriendship));
        assertFalse(user.getReceivedFriendships().contains(pendingFriendship));
    }
}
    