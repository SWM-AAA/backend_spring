package kr.co.zeppy.user.controller;

import kr.co.zeppy.global.redis.dto.LocationAndBatteryRequest;
import kr.co.zeppy.global.redis.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {

    private final RedisService redisService;

    @GetMapping("/jwt-test")
    public String jwtTest() {
        return "jwtTest 요청 성공";
    }

    @PostMapping("/v1/users/location-and-battery")
    public ResponseEntity<Void> updateLocationAndBattery(@RequestBody LocationAndBatteryRequest locationAndBatteryRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();

        boolean isSuccess = redisService.updateLocationAndBattery(userId, locationAndBatteryRequest);
        if (isSuccess) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/v1/users/friend-location-and-battery")
    public ResponseEntity<Void> getFriendLocationAndBattery() throws Exception {

        return ResponseEntity.ok().build();
    }

    @GetMapping("/v1/users/my-location-and-battery")
    public ResponseEntity<Void> getMyLocationAndBattery() throws Exception {

        return ResponseEntity.ok().build();
    }
}
