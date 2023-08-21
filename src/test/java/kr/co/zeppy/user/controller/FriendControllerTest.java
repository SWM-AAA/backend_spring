package kr.co.zeppy.user.controller;

import kr.co.zeppy.ApiDocument;
import kr.co.zeppy.SecurityConfigTest;
import kr.co.zeppy.global.aws.service.AwsS3Uploader;
import kr.co.zeppy.global.dto.ErrorResponse;
import kr.co.zeppy.global.error.ApplicationError;
import kr.co.zeppy.global.error.ApplicationException;
import kr.co.zeppy.global.error.RedisSaveException;
import kr.co.zeppy.global.jwt.service.JwtService;
import kr.co.zeppy.global.redis.dto.LocationAndBatteryRequest;
import kr.co.zeppy.global.redis.service.RedisService;
import kr.co.zeppy.user.dto.FriendshipRequest;
import kr.co.zeppy.user.dto.UserRegisterRequest;
import kr.co.zeppy.user.entity.Role;
import kr.co.zeppy.user.entity.SocialType;
import kr.co.zeppy.user.entity.User;
import kr.co.zeppy.user.repository.UserRepository;
import kr.co.zeppy.user.service.FriendService;
import kr.co.zeppy.user.service.UserService;

import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.naming.spi.DirStateFactory.Result;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import org.springframework.security.test.context.support.WithMockUser;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@WithMockUser(username = "test", roles = "USER")
@WebMvcTest(FriendController.class)
@Import(SecurityConfigTest.class)  
public class FriendControllerTest extends ApiDocument{
    private static final String API_VERSION = "/api/v1";
    private static final String RESOURCE_PATH = "/friends";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String ACCESSTOKEN = "access_token";
    private static final Long INIT_USERID = 1L;
    private static final Long INIT_FRIENDID = 2L;
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


    @MockBean
    private RedisService redisService;
    @MockBean
    private UserService userService;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private FriendService friendService;
    @MockBean
    private UserRepository userRepository;
    
    private ApplicationException userIdNotFoundException;

    private FriendshipRequest friendshipRequest;


    @BeforeEach
    void setUp() {
        friendshipRequest = FriendshipRequest.builder()
                .userId(INIT_USERID)
                .build();
        
        User user = User.builder()
                .id(INIT_USERID)
                .nickname(USER_NICKNAME)
                .imageUrl(USER_IMAGE_URL)
                .userTag(USER_TAG)
                .role(USER_ROLE)
                .socialType(USER_SOCIAL_TYPE)
                .socialId(USER_SOCIAL_ID)
                .refreshToken(USER_REFRESH_TOKEN)
                .build();
                
        User friend = User.builder()
                .id(INIT_FRIENDID)
                .nickname(FRIEND_NICKNAME)
                .imageUrl(FRIEND_IMAGE_URL)
                .userTag(FRIEND_TAG)
                .role(FRIEND_ROLE)
                .socialType(FRIEND_SOCIAL_TYPE)
                .socialId(FRIEND_SOCIAL_ID)
                .refreshToken(FRIEND_REFRESH_TOKEN)
                .build();
        
        when(jwtService.getLongUserIdFromToken(anyString())).thenReturn(INIT_USERID);
        when(userRepository.findById(INIT_USERID)).thenReturn(Optional.of(user));
        when(userRepository.findById(INIT_FRIENDID)).thenReturn(Optional.of(friend));

        userIdNotFoundException = new ApplicationException(ApplicationError.USER_NOT_FOUND);
    }

    /////////////////////////////////////////////////////////////////
    // sendFriendRequest (Method : Post) test code
    /////////////////////////////////////////////////////////////////
    @Test
    void test_Send_Friend_Request_Success() throws Exception {
        // given
        willDoNothing().given(friendService).sendFriendRequest(anyString(), any(FriendshipRequest.class));
        String jsonRequest = toJson(friendshipRequest);

        // when
        ResultActions resultActions = send_Friend_Request(jsonRequest);

        // then
        send_Friend_Request_Success(resultActions);
    }

    @Test
    void test_Send_Friend_Request_Failure() throws Exception {
        doThrow(userIdNotFoundException).when(friendService).sendFriendRequest(anyString(), any(FriendshipRequest.class));
        String jsonRequest = toJson(friendshipRequest);

        // when
        ResultActions resultActions = send_Friend_Request(jsonRequest);

        // then
        send_Friend_Request_Failure(resultActions);
    }

    private ResultActions send_Friend_Request(String jsonRequest) throws Exception {
        return mockMvc.perform(
                post(API_VERSION + RESOURCE_PATH + "/requests")
                        .header(AUTHORIZATION_HEADER, ACCESSTOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
        );
    }

    private void send_Friend_Request_Success(ResultActions resultActions) throws Exception {
        printAndMakeSnippet(resultActions.andExpect(status().isOk()),
                "send-Friends-Request-Success");
    }

    private void send_Friend_Request_Failure(ResultActions resultActions) throws Exception {
        printAndMakeSnippet(resultActions.andExpect(status().isNotFound()),
                "send-Friends-Request-Failure");
    }
}