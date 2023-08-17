package kr.co.zeppy.user.controller;

import kr.co.zeppy.global.aws.service.AwsS3Uploader;
import kr.co.zeppy.global.redis.dto.LocationAndBatteryRequest;
import kr.co.zeppy.global.redis.service.RedisService;
import kr.co.zeppy.user.dto.UserRegisterRequest;
import kr.co.zeppy.user.dto.UserRegisterRequestTest;
import kr.co.zeppy.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {

    private static final String USER_PROFILE_IMAGE_PATH = "user/profile-image";
    private final RedisService redisService;
    private final UserService userService;
    private final AwsS3Uploader awsS3Uploader;

    @GetMapping("/jwt-test")
    public String jwtTest() {
        log.info("로그인 테스트");
        return "jwtTest 요청 성공";
    }

    @PostMapping("/v1/users/register")
    public ResponseEntity<Void> userRegister(@RequestHeader("Authorization") String token,
                                @RequestBody UserRegisterRequest userRegisterRequest) {
        userService.register(token, userRegisterRequest);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/test/users/register")
    public ResponseEntity<Void> userRegisterTest(@ModelAttribute UserRegisterRequestTest userRegisterRequesttest) 
            throws IOException {
        userService.registerTest("token", "이도연#0001", userRegisterRequesttest);

        return ResponseEntity.ok().build();
    }


    @PostMapping("/v1/users/location-and-battery")
    public ResponseEntity<Void> updateUserLocationAndBattery(@RequestHeader("Authorization") String token, 
            @RequestBody LocationAndBatteryRequest locationAndBatteryRequest) {

        String userId = userService.getUserIdFromToken(token);
        redisService.updateLocationAndBattery(userId, locationAndBatteryRequest);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/test/image")
    public ResponseEntity<String> testImageUpload(@RequestParam("file") MultipartFile file) throws IOException {
        String fileName = awsS3Uploader.upload(file, USER_PROFILE_IMAGE_PATH);
        return ResponseEntity.ok().body(fileName);
    }


    // test
    @GetMapping("/v1/users/all-user-location-and-battery")
    public ResponseEntity<Map<String, Map<String, LocationAndBatteryRequest>>> getAllUserLocationAndBattery() {

        return ResponseEntity.ok().body(redisService.getAllUsersLocationAndBattery());
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
