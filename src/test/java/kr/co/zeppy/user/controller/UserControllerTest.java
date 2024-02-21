package kr.co.zeppy.user.controller;

import com.amazonaws.services.s3.AmazonS3Client;
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
import kr.co.zeppy.user.dto.*;
import kr.co.zeppy.user.entity.Role;
import kr.co.zeppy.user.entity.SocialType;
import kr.co.zeppy.user.entity.User;
import kr.co.zeppy.user.repository.FriendshipRepository;
import kr.co.zeppy.user.repository.UserRepository;
import kr.co.zeppy.user.service.NickNameService;
import kr.co.zeppy.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser(username = "test", roles = "USER")
@WebMvcTest(UserController.class)
@Import(SecurityConfigTest.class)
public class UserControllerTest extends ApiDocument {

    private static final String API_VERSION = "/api/v1";
    private static final String RESOURCE_PATH = "/users";

    private static final String LATITUDE = "37.123456";
    private static final String LONGITUDE = "127.123456";
    private static final String BATTERY = "90";
    private static final boolean IS_CHARGING = false;
    private static final String USERID = "userId";
    private static final String USER_ID = "1L";
    private static final Long USER_LONG_ID = 1L;
    private static final String NEWUSERTAG = "newUserTag";
    private static final String USERTAG = "userTag";
    private static final String TOKEN = "token";
    private static final String IMAGEURL = "imageUrl";
    private static final String PROFILE_IMAGE_NAME = "profileImage";
    private static final String FILE_NAME = "filename.jpg";
    private static final String CONTENT_TYPE = "image/jpeg";
    private static final byte[] CONTENT = "image content".getBytes();
    private static final String NICKNAME = "userNickname";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String ACCESSTOKEN = "accessToken";
    private static final String REFRESHTOKEN = "refreshToken";
    private static final String VALID_USER_TAG = "sampleUserTag";
    private static final String INVALID_USER_TAG = "invalidUserTag";

    private static final String USER_NICKNAME = "userNickname";
    private static final String USER_IMAGE_URL = "userImageUrl";
    private static final String USER_TAG = "userNickname#0001";
    private static final Role USER_ROLE = Role.USER;
    private static final SocialType USER_SOCIAL_TYPE = SocialType.KAKAO;
    private static final String USER_SOCIAL_ID = "userSocialId";
    private static final String USER_REFRESH_TOKEN = "userRefreshToken";
    private static final String NEW_NICKNAME = "newUserNickname";
    private static final String NEW_IMAGE_URL = "newImageURL";
    private static final String NEWACCESSTOKEN = "Bearer newAccessToken";
    private static final String NEWREFRESHTOKEN = "Bearer newRefreshToken";

    @MockBean
    private RedisService redisService;
    @MockBean
    private UserService userService;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private FriendshipRepository friendshipRepository;
    @MockBean
    private NickNameService nickNameService;
    @MockBean
    private AwsS3Uploader awsS3Uploader;

    @MockBean
    private AmazonS3Client amazonS3Client;

    private MockMultipartFile file;

    private ApplicationException redisUserLocationUpdateException;
    private ApplicationException internalServerException;
    private ApplicationException invalidUserTagFormat;
    private ApplicationException userNicknameNotFoundException;

    private LocationAndBatteryRequest locationAndBatteryRequest;
    private UserRegisterRequest userRegisterRequest;
    private UserInfoResponse userInfoResponse;
    private UserTagRequest userTagRequest;
    private UserRegisterResponse userRegisterResponse;
    private UserNicknameRequest userNicknameRequest;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(USER_LONG_ID)
                .nickname(USER_NICKNAME)
                .imageUrl(USER_IMAGE_URL)
                .userTag(USER_TAG)
                .role(USER_ROLE)
                .socialType(USER_SOCIAL_TYPE)
                .socialId(USER_SOCIAL_ID)
                .refreshToken(USER_REFRESH_TOKEN)
                .build();

        locationAndBatteryRequest = LocationAndBatteryRequest.builder()
                .latitude(LATITUDE)
                .longitude(LONGITUDE)
                .battery(BATTERY)
                .isCharging(IS_CHARGING)
                .build();

        file = new MockMultipartFile(PROFILE_IMAGE_NAME, FILE_NAME, CONTENT_TYPE, CONTENT);
        userRegisterRequest = UserRegisterRequest.builder()
                .nickname(NICKNAME)
                .profileImage(file)
                .build();

        userInfoResponse = UserInfoResponse.builder()
                .userId(USER_LONG_ID)
                .nickname(NICKNAME)
                .userTag(VALID_USER_TAG)
                .imageUrl(FILE_NAME)
                .isRelationship(true)
                .build();

        userTagRequest = UserTagRequest.builder()
                .userTag(NEWUSERTAG)
                .build();

        userRegisterResponse = UserRegisterResponse.builder()
                .userId(USER_LONG_ID)
                .userTag(VALID_USER_TAG)
                .imageUrl(FILE_NAME)
                .build();

        userNicknameRequest = UserNicknameRequest.builder()
                .nickname(NEW_NICKNAME)
                .build();

        given(jwtService.getStringUserIdFromToken("Bearer " + TOKEN)).willReturn(USER_ID);

        redisUserLocationUpdateException = new RedisSaveException(ApplicationError.REDIS_SERVER_UNAVAILABLE);
        internalServerException = new ApplicationException(ApplicationError.INTERNAL_SERVER_ERROR);
        invalidUserTagFormat = new ApplicationException(ApplicationError.INVALID_USER_TAG_FORMAT);
        userNicknameNotFoundException = new ApplicationException(ApplicationError.USER_NICKNAME_NOT_FOUND);
    }

    /////////////////////////////////////////////////////////////////
    // updateLocationAndBattery test code
    /////////////////////////////////////////////////////////////////
    @Test
    void test_Update_User_Location_And_Battery_Success() throws Exception {
        // given
        willDoNothing().given(redisService).updateLocationAndBattery(anyString(), any(LocationAndBatteryRequest.class));

        // when
        ResultActions resultActions = update_User_Location_And_Battery_Request();

        // then
        update_User_Location_And_Battery_Request_Success(resultActions);
    }

    @Test
    void test_Update_User_Location_And_Battery_Failure() throws Exception {
        // given
        willThrow(redisUserLocationUpdateException).given(redisService).updateLocationAndBattery(anyString(), any(LocationAndBatteryRequest.class));

        // when
        ResultActions resultActions = update_User_Location_And_Battery_Request();

        // then
        update_User_Location_And_Battery_Request_Failure(resultActions);
    }

    private ResultActions update_User_Location_And_Battery_Request() throws Exception {
        return mockMvc.perform(RestDocumentationRequestBuilders.post(API_VERSION + RESOURCE_PATH + "/location-and-battery")
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(locationAndBatteryRequest)));
    }

    private void update_User_Location_And_Battery_Request_Success(ResultActions resultActions) throws Exception {
        printAndMakeSnippet(resultActions.andExpect(status().isOk()),
                "update-Location-And-Battery-Success");
        verify(redisService, times(1)).updateLocationAndBattery(anyString(), any(LocationAndBatteryRequest.class));
    }

    private void update_User_Location_And_Battery_Request_Failure(ResultActions resultActions) throws Exception {
        printAndMakeSnippet(resultActions
                        .andExpect(status().isServiceUnavailable())
                        .andExpect(content().json(toJson(ErrorResponse.fromException(redisUserLocationUpdateException)))),
                "update-Location-And-Battery-Failure");
        verify(redisService, times(1)).updateLocationAndBattery(anyString(), any(LocationAndBatteryRequest.class));
    }

    /////////////////////////////////////////////////////////////////
    // userRegister test code
    /////////////////////////////////////////////////////////////////
    @Test
    @Disabled
    void test_User_Register_Success() throws Exception {
        // given
        Map<String, String> expectedResponse = new HashMap<>();
        expectedResponse.put("accessToken", ACCESSTOKEN);
        expectedResponse.put("userId", USER_ID);
        expectedResponse.put("userTag", NEWUSERTAG);
        expectedResponse.put("imageUrl", PROFILE_IMAGE_NAME);

        given(userService.register(anyString(), any(UserRegisterRequest.class))).willReturn(NEWUSERTAG);
        given(jwtService.createAccessToken(anyString())).willReturn(ACCESSTOKEN);
        given(userRepository.findIdByUserTag(anyString())).willReturn(Optional.of(USER_LONG_ID));
        given(userRepository.findImageUrlByUserTag(anyString())).willReturn(Optional.of(NEWUSERTAG));
//        given(userService.userRegisterBody(ACCESSTOKEN, NEWUSERTAG, USER_ID, PROFILE_IMAGE_NAME)).willReturn(expectedResponse);

        // when
        ResultActions resultActions = user_Register_Request();

        // then
        user_Register_Request_Success(resultActions);
    }


    @Test
    @Disabled
    void test_User_Register_Failure() throws Exception {
        // given
        willThrow(new RuntimeException("User registration failed")).given(userService).register(anyString(), any(UserRegisterRequest.class));

        // when
        ResultActions resultActions = user_Register_Request();

        // then
        user_Register_Request_Failure(resultActions);
    }

    private ResultActions user_Register_Request() throws Exception {
        return mockMvc.perform(RestDocumentationRequestBuilders.multipart(API_VERSION + RESOURCE_PATH + "/register")
                .file(PROFILE_IMAGE_NAME, userRegisterRequest.getProfileImage().getBytes())
                .header(AUTHORIZATION_HEADER, "Bearer " + TOKEN)
                .param(NICKNAME, userRegisterRequest.getNickname())
                .contentType(MediaType.MULTIPART_FORM_DATA));
    }

    private void user_Register_Request_Success(ResultActions resultActions) throws Exception {
        printAndMakeSnippet(resultActions.andExpect(status().isOk())
                        // .andExpect(jsonPath("$.accessToken", is(ACCESSTOKEN)))
                        .andExpect(jsonPath("$.userTag", is(NEWUSERTAG)))
                        .andExpect(jsonPath("$.userId", is(USER_ID)))
                        .andExpect(jsonPath("$.imageUrl", is(IMAGEURL))),
                "user-Register-Success");
        verify(userService, times(1)).register(anyString(), any(UserRegisterRequest.class));
    }

    private void user_Register_Request_Failure(ResultActions resultActions) throws Exception {
        printAndMakeSnippet(resultActions.andExpect(status().isInternalServerError())
                        .andExpect(content().json(toJson(ErrorResponse.fromException(internalServerException)))),
                "user-Register-Failure");
        verify(userService, times(1)).register(anyString(), any(UserRegisterRequest.class));
    }

    /////////////////////////////////////////////////////////////////
    // usertag search testcode
    /////////////////////////////////////////////////////////////////
    @Test
    void test_UserTag_Search_Success() throws Exception {
        // given
        given(userService.findUserTag(any(UserTagRequest.class), anyLong())).willReturn(userInfoResponse);

        // when
        ResultActions resultActions = userTag_Search_Request();

        // then
        userTag_Search_Request_Success(resultActions);
    }

    @Test
    void test_UserTag_Search_Failure_InvalidFormat() throws Exception {
        // given
        given(userService.findUserTag(any(UserTagRequest.class), anyLong())).willThrow(invalidUserTagFormat);

        // when
        ResultActions resultActions = userTag_Search_Request();

        // then
        userTag_Search_Request_Failure(resultActions);
    }

    private ResultActions userTag_Search_Request() throws Exception {
        return mockMvc.perform(RestDocumentationRequestBuilders.post(API_VERSION + RESOURCE_PATH + "/search/usertag")
                .header("Authorization", "Bearer " + TOKEN)
                .content(toJson(userTagRequest))
                .contentType(MediaType.APPLICATION_JSON));
    }

    private void userTag_Search_Request_Success(ResultActions resultActions) throws Exception {
        printAndMakeSnippet(resultActions.andExpect(status().isOk())
                        .andExpect(jsonPath("$.userId", is(userInfoResponse.getUserId().intValue())))
                        .andExpect(jsonPath("$.nickname", is(userInfoResponse.getNickname())))
                        .andExpect(jsonPath("$.userTag", is(userInfoResponse.getUserTag())))
                        .andExpect(jsonPath("$.imageUrl", is(userInfoResponse.getImageUrl()))),
                "userTag-Search-Success");
        verify(userService, times(1)).findUserTag(any(UserTagRequest.class), anyLong());
    }

    private void userTag_Search_Request_Failure(ResultActions resultActions) throws Exception {
        printAndMakeSnippet(resultActions.andExpect(status().isBadRequest())
                        .andExpect(content().json(toJson(ErrorResponse.fromException(invalidUserTagFormat)))),
                "userTag-Search-Failure");
        verify(userService, times(1)).findUserTag(any(UserTagRequest.class), anyLong());
    }

    /////////////////////////////////////////////////////////////////
    // updateMyNickname (Method : patch) test code || (url : nickname)
    /////////////////////////////////////////////////////////////////
    @Test
    void test_update_User_Nickname_Success() throws Exception {
        // given
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put(ACCESSTOKEN, "newAccessToken");
        tokenMap.put(REFRESHTOKEN, "newRefreshToken");

        when(userService.updateUserNickname(anyString(), any(UserNicknameRequest.class))).thenReturn(tokenMap);

        // when
        ResultActions resultActions = update_User_Nickname();

        // then
        update_User_Nickname_Success(resultActions);
    }

    @Test
    void test_update_User_Nickname_Failure() throws Exception {
        // given
        doThrow(userNicknameNotFoundException).when(userService).updateUserNickname(anyString(), any(UserNicknameRequest.class));

        // when
        ResultActions resultActions = update_User_Nickname();

        // then
        update_User_Nickname_Failure(resultActions);
    }

    private ResultActions update_User_Nickname() throws Exception {
        return mockMvc.perform(
                RestDocumentationRequestBuilders.patch(API_VERSION + RESOURCE_PATH + "/nickname")
                        .header(AUTHORIZATION_HEADER, "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(userNicknameRequest))
        );
    }

    void update_User_Nickname_Success(ResultActions resultActions) throws Exception {
        printAndMakeSnippet(resultActions.andExpect(status().isOk())
                .andExpect(header().string("Authorization", NEWACCESSTOKEN))
                .andExpect(header().string("Authorization-refresh", NEWREFRESHTOKEN)),
                "update-User-Nickname-Request-Success");
        verify(userService, times(1)).updateUserNickname(anyString(), any(UserNicknameRequest.class));
    }

    void update_User_Nickname_Failure(ResultActions resultActions) throws Exception {
        printAndMakeSnippet(resultActions.andExpect(status().isNotFound())
                        .andExpect(content().json(toJson(ErrorResponse.fromException(userNicknameNotFoundException)))),
                "update-User-Nickname-Request-Failure");
        verify(userService, times(1)).updateUserNickname(anyString(), any(UserNicknameRequest.class));
    }


    /////////////////////////////////////////////////////////////////
    // updateMyImage (Method : patch) test code || (url : image)
    /////////////////////////////////////////////////////////////////
    @Test
    void test_update_User_Image_Success() throws Exception {
        // given
//        when(userService.updateUserImage(anyString(), any(MockMultipartFile.class))).thenReturn(NEW_IMAGE_URL);
        doAnswer(invocation -> {
            user.updateImageUrl(NEW_IMAGE_URL);
            return null;
        }).when(userService).updateUserImage(anyString(), any(MockMultipartFile.class));

        // when
        ResultActions resultActions = update_User_Image();

        // then
        update_User_Image_Success(resultActions);
    }

    @Test
    void test_update_User_Image_Failure() throws Exception {
        // given
        doThrow(internalServerException).when(userService).updateUserImage(anyString(), any(MultipartFile.class));

        // when
        ResultActions resultActions = update_User_Image();

        // then
        update_User_Image_Failure(resultActions);
    }

    private ResultActions update_User_Image() throws Exception {
        MockMultipartHttpServletRequestBuilder builder = RestDocumentationRequestBuilders.multipart(API_VERSION + RESOURCE_PATH + "/image");
        builder.with(new RequestPostProcessor() {
            @Override
            public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
                request.setMethod("PATCH");
                return request;
            }
        });

        return mockMvc.perform(builder
                .file("File", file.getBytes())
                .header(AUTHORIZATION_HEADER, "Bearer " + ACCESSTOKEN)
                .contentType(MediaType.MULTIPART_FORM_DATA)
        );
    }

    void update_User_Image_Success(ResultActions resultActions) throws Exception {
        printAndMakeSnippet(resultActions.andExpect(status().isOk()),
                "update-User-Image-Success");
        verify(userService, times(1)).updateUserImage(anyString(), any(MockMultipartFile.class));
    }

    void update_User_Image_Failure(ResultActions resultActions) throws Exception {
        printAndMakeSnippet(resultActions.andExpect(status().isInternalServerError())
                        .andExpect(content().json(toJson(ErrorResponse.fromException(internalServerException)))),
                "update-User-Image-Failure");
        verify(userService, times(1)).updateUserImage(anyString(), any(MockMultipartFile.class));
    }

    /////////////////////////////////////////////////////////////////
    // removeMe (Method : post) test code || (url : remove-user)
    /////////////////////////////////////////////////////////////////
    @Test
    @WithMockUser
    void test_delete_User_Success() throws Exception {
        // given
        doAnswer(invocation -> {
            userRepository.delete(any());
            return null;
        }).when(userService).deleteUser(anyString());

        // when
        ResultActions resultActions = delete_User();

        // then
        delete_User_Success(resultActions);
    }

    @Test
    @WithMockUser
    void test_delete_User_Failure() throws Exception {
        // given
        doThrow(internalServerException).when(userService).deleteUser(anyString());

        // when
        ResultActions resultActions = delete_User();

        // then
        delete_User_Failure(resultActions);
    }

    private ResultActions delete_User() throws Exception {
        return mockMvc.perform(
                RestDocumentationRequestBuilders.patch(API_VERSION + RESOURCE_PATH)
                        .header(AUTHORIZATION_HEADER, "Bearer " + ACCESSTOKEN));
    }

    void delete_User_Success(ResultActions resultActions) throws Exception {
        printAndMakeSnippet(resultActions.andExpect(status().isOk()),
                "remove-User-Request-Success");
        verify(userService, times(1)).deleteUser(anyString());
    }

    void delete_User_Failure(ResultActions resultActions) throws Exception {
        printAndMakeSnippet(resultActions.andExpect(status().isInternalServerError())
                        .andExpect(content().json(toJson(ErrorResponse.fromException(internalServerException)))),
                "remove-User-Failure");
        verify(userService, times(1)).deleteUser(anyString());
    }
}