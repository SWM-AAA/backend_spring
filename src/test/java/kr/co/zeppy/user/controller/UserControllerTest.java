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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.security.test.context.support.WithMockUser;

@WebMvcTest(UserController.class)
@Import(SecurityConfigTest.class)
public class UserControllerTest extends ApiDocument {

    private static final String API_VERSION = "/api/v1";
    private static final String RESOURCE_PATH = "/users";

    @MockBean
    private RedisService redisService;

    private LocationAndBatteryRequest locationAndBatteryRequest;

    @BeforeEach
    void setUp() {
        locationAndBatteryRequest = LocationAndBatteryRequest.builder()
                .latitude("37.123456")
                .longitude("127.123456")
                .battery("90")
                .isCharging(false)
                .build();
    }

    @WithMockUser
    @Test
    void testUpdateLocationAndBattery() throws Exception {
        ResultActions resultActions = this.mockMvc.perform(
                post(API_VERSION + RESOURCE_PATH + "/location-and-battery")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(locationAndBatteryRequest)))
                .andExpect(status().isOk());

        verify(redisService, times(1)).updateLocationAndBattery(any(LocationAndBatteryRequest.class));

        printAndMakeSnippet(resultActions, "updateLocationAndBattery");
    }
}
