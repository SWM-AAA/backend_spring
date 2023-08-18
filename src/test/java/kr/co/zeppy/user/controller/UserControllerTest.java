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
import kr.co.zeppy.user.dto.UserRegisterRequest;
import kr.co.zeppy.user.service.UserService;

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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import org.springframework.security.test.context.support.WithMockUser;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


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
    private static final String USER_ID = "1";
    private static final String TOKEN = "token";
    private static final String PROFILE_IMAGE_NAME = "profileimage";
    private static final String FILE_NAME = "filename.jpg";
    private static final String CONTENT_TYPE = "image/jpeg";
    private static final byte[] CONTENT = "image content".getBytes();
    private static final String NICKNAME = "userNickname";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String ACCESSTOKEN = "access_token";

    @MockBean
    private RedisService redisService;
    @MockBean
    private UserService userService;
    @MockBean
    private JwtService jwtService;

    @MockBean
    private AwsS3Uploader awsS3Uploader;

    private LocationAndBatteryRequest locationAndBatteryRequest;

    private ApplicationException redisUserLocationUpdateException;

    private ApplicationException internalServerException;

    private UserRegisterRequest userRegisterRequest;

    private MockMultipartFile file;
    

    @BeforeEach
    void setUp() {
        locationAndBatteryRequest = LocationAndBatteryRequest.builder()
                .latitude(LATITUDE)
                .longitude(LONGITUDE)
                .battery(BATTERY)
                .isCharging(IS_CHARGING)
                .build();
        
        file = new MockMultipartFile(PROFILE_IMAGE_NAME, FILE_NAME, CONTENT_TYPE, CONTENT);
        userRegisterRequest = UserRegisterRequest.builder()
                .nickname(NICKNAME)
                .profileimage(file)
                .build();
        
        given(userService.getUserIdFromToken("Bearer " + TOKEN)).willReturn(USER_ID);
        
        redisUserLocationUpdateException = new RedisSaveException(ApplicationError.REDIS_SERVER_UNAVAILABLE);
        internalServerException = new ApplicationException(ApplicationError.INTERNAL_SERVER_ERROR);
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
        return mockMvc.perform(post(API_VERSION + RESOURCE_PATH + "/location-and-battery")
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(locationAndBatteryRequest)));
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
    void test_User_Register_Success() throws Exception {
        // given
        given(userService.register(anyString(), any(UserRegisterRequest.class))).willReturn("newUserTag");
        given(jwtService.createAccessToken("newUserTag")).willReturn("testAccessToken");
        
        // when
        ResultActions resultActions = user_Register_Request();
        
        // then
        user_Register_Request_Success(resultActions);
    }

    
    @Test
    void test_User_Register_Failure() throws Exception {
        // given
        willThrow(new RuntimeException("User registration failed")).given(userService).register(anyString(), any(UserRegisterRequest.class));
    
        // when
        ResultActions resultActions = user_Register_Request();
    
        // then
        user_Register_Request_Failure(resultActions);
    }
    
    private ResultActions user_Register_Request() throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.multipart(API_VERSION + RESOURCE_PATH + "/register")
                .file("profileimage", userRegisterRequest.getProfileimage().getBytes())
                .header(AUTHORIZATION_HEADER, "Bearer " + TOKEN)
                .param("nickname", userRegisterRequest.getNickname())
                .contentType(MediaType.MULTIPART_FORM_DATA));
    }
    

    private void user_Register_Request_Success(ResultActions resultActions) throws Exception {
        printAndMakeSnippet(resultActions.andExpect(status().isOk())
                            .andExpect(jsonPath("$." + ACCESSTOKEN, is("testAccessToken"))),
                            "user-Register-Success");
        verify(userService, times(1)).register(anyString(), any(UserRegisterRequest.class));
    }
    
    
    
    private void user_Register_Request_Failure(ResultActions resultActions) throws Exception {
        printAndMakeSnippet(resultActions.andExpect(status().isInternalServerError())
                        .andExpect(content().json(toJson(ErrorResponse.fromException(internalServerException)))),
                        "user-Register-Failure");
        verify(userService, times(1)).register(anyString(), any(UserRegisterRequest.class));
    }
}