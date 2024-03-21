package kr.co.zeppy.location.controller;

import kr.co.zeppy.ApiDocument;
import kr.co.zeppy.SecurityConfigTest;
import kr.co.zeppy.global.dto.ApiResponse;
import kr.co.zeppy.global.dto.ErrorResponse;
import kr.co.zeppy.global.error.ApplicationError;
import kr.co.zeppy.global.error.ApplicationException;
import kr.co.zeppy.global.jwt.service.JwtService;
import kr.co.zeppy.location.dto.CurrentLocationModeResponse;
import kr.co.zeppy.location.dto.FriendInfo;
import kr.co.zeppy.location.dto.LocationModeTimerResponse;
import kr.co.zeppy.user.dto.UserFriendInfoResponse;
import kr.co.zeppy.user.entity.Role;
import kr.co.zeppy.user.entity.SocialType;
import kr.co.zeppy.user.entity.User;
import kr.co.zeppy.location.service.LocationModeService;

import kr.co.zeppy.user.repository.UserRepository;
import kr.co.zeppy.user.service.FriendService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WithMockUser(username = "test", roles = "USER")
@WebMvcTest(LocationModeController.class)
@Import(SecurityConfigTest.class)
public class LocationModeControllerTest extends ApiDocument{

    private static final String API_VERSION = "/api/v1";
    private static final String RESOURCE_PATH = "/mode";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String ACCESSTOKEN = "accessToken";
    private static final Long INIT_USERID = 1L;
    private static final String USER_NICKNAME = "UserNickname";
    private static final String USER_IMAGE_URL = "userImageUrl";
    private static final String USER_TAG = "User#0001";
    private static final Role USER_ROLE = Role.USER;
    private static final SocialType USER_SOCIAL_TYPE = SocialType.KAKAO;
    private static final String USER_SOCIAL_ID = "userSocialId";
    private static final String USER_REFRESH_TOKEN = "userRefreshToken";

    private static final Long INIT_FRIENDID = 2L;
    private static final String FRIEND_NICKNAME = "FriendNickname";
    private static final String FRIEND_IMAGE_URL = "friendImageUrl";
    private static final String FRIEND_TAG = "Friend#0001";
    private static final Role FRIEND_ROLE = Role.USER;
    private static final SocialType FRIEND_SOCIAL_TYPE = SocialType.GOOGLE;
    private static final String FRIEND_SOCIAL_ID = "friendSocialId";
    private static final String FRIEND_REFRESH_TOKEN = "friendRefreshToken";

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private LocationModeService locationModeService;

    private List<Integer> timeList;
    private LocationModeTimerResponse locationModeTimerResponse;
    private CurrentLocationModeResponse currentLocationModeResponse;

    private ApplicationException internalServerException;

    private User user;
    private User friend;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(INIT_USERID)
                .nickname(USER_NICKNAME)
                .imageUrl(USER_IMAGE_URL)
                .userTag(USER_TAG)
                .role(USER_ROLE)
                .socialType(USER_SOCIAL_TYPE)
                .socialId(USER_SOCIAL_ID)
                .refreshToken(USER_REFRESH_TOKEN)
                .activated(true)
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
                .activated(true)
                .build();

        FriendInfo friendInfo = FriendInfo.builder()
                .userId(INIT_FRIENDID)
                .userTag(FRIEND_TAG)
                .imageUrl(FRIEND_IMAGE_URL)
                .build();

        currentLocationModeResponse = CurrentLocationModeResponse.builder()
                .accurate(new ArrayList<>())
                .ambiguous(List.of(friendInfo))
                .pinned(new ArrayList<>())
                .build();

        timeList = Arrays.asList(2, 4, 8, 24);
        locationModeTimerResponse = LocationModeTimerResponse.builder()
                .times(timeList)
                .build();

        internalServerException = new ApplicationException(ApplicationError.INTERNAL_SERVER_ERROR);

        when(jwtService.getLongUserIdFromToken(anyString())).thenReturn(INIT_USERID);
        when(userRepository.findById(INIT_USERID)).thenReturn(Optional.of(user));
    }

    /////////////////////////////////////////////////////////////////
    // getTimesRequest (Method : get) test code || (url : time)
    /////////////////////////////////////////////////////////////////

    @WithMockUser
    @Test
    void test_Get_Times_Request_Success() throws Exception {
        // given
        when(locationModeService.getTimes()).thenReturn(locationModeTimerResponse);

        // when
        ResultActions resultActions = get_Times_Request();

        // then
        get_Times_Request_Success(resultActions);
    }

    @WithMockUser
    @Test
    void test_Get_Times_Request_Failure() throws Exception {
        // given
        doThrow(internalServerException).when(locationModeService).getTimes();

        // when
        ResultActions resultActions = get_Times_Request();

        // then
        get_Times_Request_Failure(resultActions);
    }

    private ResultActions get_Times_Request() throws Exception {
        return mockMvc.perform(
                RestDocumentationRequestBuilders.get(API_VERSION + RESOURCE_PATH + "/time")
                        .header(AUTHORIZATION_HEADER, ACCESSTOKEN));
    }

    private void get_Times_Request_Success(ResultActions resultActions) throws Exception {
        printAndMakeSnippet(resultActions.andExpect(status().isOk())
                        .andExpect(content().json(toJson(ApiResponse.success(locationModeTimerResponse)))),
                "get-Times-Request-Success");
    }

    private void get_Times_Request_Failure(ResultActions resultActions) throws Exception {
        printAndMakeSnippet(resultActions.andExpect(status().isInternalServerError())
                        .andExpect(content().json(toJson(ApiResponse.failure(ErrorResponse.fromException(internalServerException))))),
                "get-Times-Request-Failure");
    }

    /////////////////////////////////////////////////////////////////
    // getCurrentLocationModeRequest (Method : get) test code
    /////////////////////////////////////////////////////////////////

    @WithMockUser
    @Test
    void test_Get_Current_Location_Mode_Request_Success() throws Exception {
        // given
        when(locationModeService.getLocationMode(anyLong())).thenReturn(currentLocationModeResponse);

        // when
        ResultActions resultActions = get_Current_Location_Mode_Request();

        // then
        get_Current_Location_Mode_Request_Success(resultActions);
    }

    @WithMockUser
    @Test
    void test_Get_Current_Location_Mode_Request_Failure() throws Exception {
        // given
        doThrow(internalServerException).when(locationModeService).getLocationMode(anyLong());

        // when
        ResultActions resultActions = get_Current_Location_Mode_Request();

        // then
        get_Current_Location_Mode_Request_Failure(resultActions);
    }

    private ResultActions get_Current_Location_Mode_Request() throws Exception {
        return mockMvc.perform(
                RestDocumentationRequestBuilders.get(API_VERSION + RESOURCE_PATH)
                        .header(AUTHORIZATION_HEADER, ACCESSTOKEN));
    }

    private void get_Current_Location_Mode_Request_Success(ResultActions resultActions) throws Exception {
        printAndMakeSnippet(resultActions.andExpect(status().isOk())
                        .andExpect(content().json(toJson(ApiResponse.success(currentLocationModeResponse)))),
                "get-Current-Location-Mode-Request-Success");
    }

    private void get_Current_Location_Mode_Request_Failure(ResultActions resultActions) throws Exception {
        printAndMakeSnippet(resultActions.andExpect(status().isInternalServerError())
                        .andExpect(content().json(toJson(ApiResponse.failure(ErrorResponse.fromException(internalServerException))))),
                "get-Current-Location-Mode-Request-Failure");
    }
}
