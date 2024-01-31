package kr.co.zeppy.user.controller;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import kr.co.zeppy.global.annotation.UserId;
import kr.co.zeppy.global.aws.service.AwsS3Uploader;
import kr.co.zeppy.global.error.ApplicationError;
import kr.co.zeppy.global.error.ApplicationException;
import kr.co.zeppy.global.jwt.service.JwtService;
import kr.co.zeppy.global.redis.dto.LocationAndBatteryRequest;
import kr.co.zeppy.global.redis.service.RedisService;
import kr.co.zeppy.user.dto.*;
import kr.co.zeppy.user.entity.User;
import kr.co.zeppy.user.repository.UserRepository;
import kr.co.zeppy.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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

    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    public String bucket;

    @GetMapping("/test/jwt-test")
    public String jwtTest() {
        log.info("로그인 테스트");
        return "jwtTest 요청 성공";
    }

    // todo : 사용자 이미지도 body에 포함시켜서 보내주기
    @PostMapping("/v1/users/register")
    public ResponseEntity<UserRegisterResponse> userRegister(@RequestHeader("Authorization") String token,
                                                             @ModelAttribute UserRegisterRequest userRegisterRequest)
            throws IOException {

        String newUserTag = userService.register(token, userRegisterRequest);
        String accessToken = jwtService.createAccessToken(newUserTag);
        Long userId = userRepository.findIdByUserTag(newUserTag)
                .orElseThrow(() -> new ApplicationException(ApplicationError.USER_TAG_NOT_FOUND));
        String userImageUrl = userRepository.findImageUrlByUserTag(newUserTag)
                .map(String::valueOf)
                .orElseThrow(() -> new ApplicationException(ApplicationError.USER_IMAGE_URL_NOT_FOUND));

        UserRegisterResponse userRegisterResponse = userService.userRegisterBody(accessToken, newUserTag, userId, userImageUrl);

        return ResponseEntity.ok(userRegisterResponse);
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

    // todo : testcode 미작성
    // usertag로 사용자 검색후 반환
    @PostMapping("/v1/users/search/usertag")
    public ResponseEntity<UserInfoResponse> searchUserTag(@UserId Long userId, @RequestBody UserTagRequest userTagRequest) {
        UserInfoResponse userInfoResponse = userService.findUserTag(userTagRequest, userId);

        return ResponseEntity.ok(userInfoResponse);
    }

    // test
    @PostMapping("/test/image")
    public ResponseEntity<String> testImageUpload(@RequestParam("file") MultipartFile file) throws IOException {
        log.info("테스트");
        String fileName = awsS3Uploader.upload(file, "test/test-image");
        return ResponseEntity.ok().body(fileName);
    }

    @PostMapping("/test/image2")
    public ResponseEntity<String> testimage2(@RequestParam("file") MultipartFile file) throws IOException {
        String userId = "/0";
        String fileName = awsS3Uploader.newUpload(file, "user" + userId + "/profile");
        return ResponseEntity.ok().body(fileName);
    }

    @PostMapping("/test/image3")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // 파일명 얻기
            String fileName = file.getOriginalFilename();

            // 파일 URL 구성
            String fileUrl = "https://" + bucket + "/test/" + fileName; // 'test'와 파일명 사이에 슬래시 추가

            // 메타데이터 설정
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());
            log.info(bucket);

            // S3에 파일 업로드
            amazonS3Client.putObject(bucket, "test/" + fileName, file.getInputStream(), metadata); // 파일 경로에 'test/' 추가

            return ResponseEntity.ok(fileUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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


    @GetMapping("/v1/users/my-location-and-battery")
    public ResponseEntity<Void> getMyLocationAndBattery() throws Exception {

        return ResponseEntity.ok().build();
    }
}
