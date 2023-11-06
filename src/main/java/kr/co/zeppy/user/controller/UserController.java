package kr.co.zeppy.user.controller;

import kr.co.zeppy.global.annotation.UserId;
import kr.co.zeppy.global.aws.service.AwsS3Uploader;
import kr.co.zeppy.global.error.ApplicationError;
import kr.co.zeppy.global.error.ApplicationException;
import kr.co.zeppy.global.error.NotFoundException;
import kr.co.zeppy.global.jwt.service.JwtService;
import kr.co.zeppy.global.redis.dto.LocationAndBatteryRequest;
import kr.co.zeppy.global.redis.service.RedisService;
import kr.co.zeppy.user.dto.UserInfoResponse;
import kr.co.zeppy.user.dto.UserPinInformationResponse;
import kr.co.zeppy.user.dto.UserRegisterRequest;
import kr.co.zeppy.user.dto.UserTagRequest;
import kr.co.zeppy.user.entity.User;
import kr.co.zeppy.user.repository.UserRepository;
import kr.co.zeppy.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;

import org.springframework.context.annotation.Profile;
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
    private final JwtService jwtService;
    private final UserRepository userRepository;

    
    @GetMapping("/test/jwt-test")
    public String jwtTest() {
        log.info("로그인 테스트");
        return "jwtTest 요청 성공";
    }

    // todo : 사용자 이미지도 body에 포함시켜서 보내주기
    @PostMapping("/v1/users/register")
    public ResponseEntity<Map<String, String>> userRegister(@RequestHeader("Authorization") String token,
            @ModelAttribute UserRegisterRequest userRegisterRequest) 
            throws IOException {
        
        String newUserTag = userService.register(token, userRegisterRequest);
        String accessToken = jwtService.createAccessToken(newUserTag);
        String userId = userRepository.findIdByUserTag(newUserTag)
                .map(String::valueOf)
                .orElseThrow(() -> new ApplicationException(ApplicationError.USER_TAG_NOT_FOUND));
        String userImageUrl = userRepository.findImageUrlByUserTag(newUserTag).toString();

        Map<String, String> responseBody = userService.userRegisterBody(accessToken, newUserTag, userId, userImageUrl);

        return ResponseEntity.ok(responseBody);
    }

    // test controller
    @Profile("local")
    @PostMapping("/test/users/register")
    public ResponseEntity<Map<String, String>> userTestToken(
        @RequestParam String nickName,
        @RequestParam String userTag
    ) {

        if (!userRepository.existsByUserTag(userTag)) {
            userService.testUserRegister(nickName, userTag);
        }
        User user = userRepository.findByUserTag(userTag) 
                .orElseThrow(() -> new ApplicationException(ApplicationError.USER_TAG_NOT_FOUND));
        log.info(user.getUserTag());
        log.info(user.getId().toString());
        String accessToken = jwtService.createAccessToken(userTag);
        String refreshToken = jwtService.createRefreshToken();

        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("accessToken", accessToken);
        responseBody.put("refreshToken", refreshToken);
        user.updateRefreshToken(refreshToken);

        return ResponseEntity.ok(responseBody);
    }


    @PostMapping("/v1/users/location-and-battery")
    public ResponseEntity<Void> updateUserLocationAndBattery(@RequestHeader("Authorization") String token, 
            @RequestBody LocationAndBatteryRequest locationAndBatteryRequest) {

        String userId = jwtService.getStringUserIdFromToken(token);
        redisService.updateLocationAndBattery(userId, locationAndBatteryRequest);

        return ResponseEntity.ok().build();
    }

    // testcode 미작성 usertag로 사용자 검색후 반환
    @PostMapping("/v1/users/search/usertag")
    public ResponseEntity<UserInfoResponse> searchUserTag(@RequestBody UserTagRequest userTagRequest) {
        UserInfoResponse userInfoResponse = userService.findUserTag(userTagRequest);
    
        return ResponseEntity.ok(userInfoResponse);
    }

    // test
    @PostMapping("/test/image")
    public ResponseEntity<String> testImageUpload(@RequestParam("file") MultipartFile file) throws IOException {
        String fileName = awsS3Uploader.upload(file, USER_PROFILE_IMAGE_PATH+"/1");
        return ResponseEntity.ok().body(fileName);
    }

    // test
    @GetMapping("/v1/users/all-user-location-and-battery")
    public ResponseEntity<Map<String, Map<String, LocationAndBatteryRequest>>> getAllUserLocationAndBattery() {

        return ResponseEntity.ok().body(redisService.getAllUsersLocationAndBattery());
    }

    // test
    @GetMapping("/test/users/all-user-information")
    public ResponseEntity<List<UserPinInformationResponse>> getAllUserInformationTest() {
        List<UserPinInformationResponse> allUserInformation = userService.getAllUserInformation();

        return ResponseEntity.ok().body(allUserInformation);
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
