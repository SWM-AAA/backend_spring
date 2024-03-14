package kr.co.zeppy.location.controller;

import kr.co.zeppy.global.dto.ApiResponse;
import kr.co.zeppy.location.dto.LocationModeTimerResponse;
import kr.co.zeppy.location.service.LocationModeService;
import kr.co.zeppy.user.dto.UserRegisterByUsernameRequest;
import kr.co.zeppy.user.dto.UserRegisterByUsernameResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class LocationModeController {

    private final LocationModeService locationModeService;

    @GetMapping("/v1/mode/time")
    public ResponseEntity<ApiResponse<LocationModeTimerResponse>> getLocationModeTimes() {

        LocationModeTimerResponse response = locationModeService.getTimes();
        return ResponseEntity.ok().body(ApiResponse.success(response));
    }
}
