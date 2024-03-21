package kr.co.zeppy.location.controller;

import io.swagger.v3.core.model.ApiDescription;
import kr.co.zeppy.global.annotation.UserId;
import kr.co.zeppy.global.dto.ApiResponse;
import kr.co.zeppy.location.dto.UpdateLocationModeRequest;
import kr.co.zeppy.location.dto.CurrentLocationModeResponse;
import kr.co.zeppy.location.dto.LocationModeTimerResponse;
import kr.co.zeppy.location.service.LocationModeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/v1/mode")
    public ResponseEntity<ApiResponse<CurrentLocationModeResponse>> getCurrentLocationMode(@UserId Long userId) {

        CurrentLocationModeResponse response = locationModeService.getLocationMode(userId);
        return ResponseEntity.ok().body(ApiResponse.success(response));
    }

    @PatchMapping("/v1/mode")
    public ResponseEntity<ApiResponse<CurrentLocationModeResponse>> updateLocationMode(@UserId Long userId, @RequestBody UpdateLocationModeRequest updateLocationModeRequest) {

        CurrentLocationModeResponse response = locationModeService.updateMode(userId, updateLocationModeRequest);
        return ResponseEntity.ok().body(ApiResponse.success(response));
    }
}
