package kr.co.zeppy.user.controller;

import kr.co.zeppy.ApiDocument;
import kr.co.zeppy.SecurityConfigTest;
import kr.co.zeppy.global.redis.dto.LocationAndBatteryRequest;
import kr.co.zeppy.global.redis.service.RedisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.security.test.context.support.WithMockUser;

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

    @MockBean
    private RedisService redisService;

    private LocationAndBatteryRequest locationAndBatteryRequest;

    @BeforeEach
    void setUp() {
        locationAndBatteryRequest = LocationAndBatteryRequest.builder()
                .latitude(LATITUDE)
                .longitude(LONGITUDE)
                .battery(BATTERY)
                .isCharging(IS_CHARGING)
                .build();
    }

    @Test
    void test_Update_Location_And_Battery_Success() throws Exception {
        // given
        willReturn(true).given(redisService).updateLocationAndBattery(anyString(), any(LocationAndBatteryRequest.class));

        // when
        ResultActions resultActions = update_Location_And_Battery_Request();

        // then
        update_Location_And_Battery_Request_Success(resultActions);
    }

    @Test
    void test_Update_Location_And_Battery_Failure() throws Exception {
        // given
        willReturn(false).given(redisService).updateLocationAndBattery(anyString(), any(LocationAndBatteryRequest.class));

        // when
        ResultActions resultActions = update_Location_And_Battery_Request();

        // then
        update_Location_And_Battery_Request_Failure(resultActions);
    }

    private ResultActions update_Location_And_Battery_Request() throws Exception {
        return mockMvc.perform(post(API_VERSION + RESOURCE_PATH + "/location-and-battery")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(locationAndBatteryRequest)));
    }

    private void update_Location_And_Battery_Request_Success(ResultActions resultActions) throws Exception {
        printAndMakeSnippet(resultActions.andExpect(status().isOk()), "update-Location-And-Battery-Success");
        verify(redisService, times(1)).updateLocationAndBattery(anyString(), any(LocationAndBatteryRequest.class));
    }

    private void update_Location_And_Battery_Request_Failure(ResultActions resultActions) throws Exception {
        printAndMakeSnippet(resultActions.andExpect(status().isBadRequest()), "update-Location-And-Battery-Failure");
        verify(redisService, times(1)).updateLocationAndBattery(anyString(), any(LocationAndBatteryRequest.class));
    }
}