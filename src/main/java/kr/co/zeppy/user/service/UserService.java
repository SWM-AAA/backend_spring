package kr.co.zeppy.user.service;

import jakarta.transaction.Transactional;
import kr.co.zeppy.global.aws.service.AwsS3Uploader;
import kr.co.zeppy.global.dto.ApiResponse;
import kr.co.zeppy.global.error.ApplicationError;
import kr.co.zeppy.global.error.ApplicationException;
import kr.co.zeppy.global.jwt.service.JwtService;
import kr.co.zeppy.user.dto.*;
import kr.co.zeppy.user.entity.*;
import kr.co.zeppy.user.repository.FriendshipRepository;
import kr.co.zeppy.user.repository.NickNameRepository;
import kr.co.zeppy.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private static final String S3_USER_PROFILE_BASE_PATH = "user/profile-image/";
    private static final String S3_USER_PROFILE_LAST_PATH = "profile";
    private static final String ACCESSTOKEN = "accessToken";
    private static final String REFRESHTOKEN = "refreshToken";
    private static final String USERTAG = "userTag";
    private static final String USERID = "userId";
    private static final String IMAGEURL = "imageUrl";

    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final JwtService jwtService;
    private final AwsS3Uploader awsS3Uploader;
    private final NickNameService nickNameService;
    private final NickNameRepository nickNameRepository;
    private final PasswordEncoder passwordEncoder;


    // test
    public List<UserPinInformationResponse> getAllUserInformation() {
        return userRepository.findAll().stream()
            .map(this::toUserPinInformationResponse)
            .collect(Collectors.toList());
    }

    // test
    public UserPinInformationResponse toUserPinInformationResponse(User user) {
        return UserPinInformationResponse.builder()
            .userId(user.getId())
            .userTag(user.getUserTag())
            .nickname(user.getNickname())
            .imageUrl(user.getImageUrl())
            .build();
    }

    // test
    public void testUserRegister(String nickname, String userTag) {
        String randomSocialId = UUID.randomUUID().toString();

        User user = User.builder()
            .nickname(nickname)
            .userTag(userTag)
            .socialType(SocialType.KAKAO)
            .socialId(randomSocialId)
            .role(Role.GUEST)
            .imageUrl("test")
            .activated(true)
            .build();

        userRepository.save(user);
    }

    public UserRegisterByUsernameResponse registerByUsername(UserRegisterByUsernameRequest userRegisterByUsernameRequest) throws Exception {

        if (userRepository.findByUsername(userRegisterByUsernameRequest.getUsername()).isPresent()) {
            throw new ApplicationException(ApplicationError.USERNAME_DUPLICATED);
        }

        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();
        String userNickname = random.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        String newUserTag = nickNameService.getUserTagFromNickName(userNickname);
        String userImageUrl = "https://zeppy-s3.s3.ap-northeast-2.amazonaws.com/user/profile-image/1profile";

        User user = User.builder()
                .username(userRegisterByUsernameRequest.getUsername())
                .password(userRegisterByUsernameRequest.getPassword())
                .nickname(userNickname)
                .userTag(newUserTag)
                .imageUrl(userImageUrl)
                .role(Role.USER)
                .activated(true)
                .build();

        user.passwordEncode(passwordEncoder);
        userRepository.save(user);

        String accessToken = jwtService.createAccessToken(user.getUserTag());
        String refreshToken = jwtService.createRefreshToken();
        user.updateRefreshToken(refreshToken);

        return UserRegisterByUsernameResponse.builder()
                .accessToken(accessToken)
                .refreshToken(user.getRefreshToken())
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .userTag(user.getUserTag())
                .imageUrl(user.getImageUrl())
                .build();
    }

    public String register(String Token, UserRegisterRequest userRegisterRequest)
            throws IOException {
        String userTag = jwtService.extractUserTagFromToken(Token)
                .orElseThrow(() -> new ApplicationException(ApplicationError.USER_TAG_NOT_FOUND));

        User user = userRepository.findByUserTag(userTag)
                .orElseThrow(() -> new ApplicationException(ApplicationError.USER_NOT_FOUND));

        MultipartFile file = userRegisterRequest.getProfileImage();
        String newUserTag = nickNameService.getUserTagFromNickName(userRegisterRequest.getNickname());

        String fileName = awsS3Uploader.newUpload(file
                ,S3_USER_PROFILE_BASE_PATH + user.getId() + S3_USER_PROFILE_LAST_PATH);

        user.updateUserTag(newUserTag);
        user.updateNickname(userRegisterRequest.getNickname());
        user.updateImageUrl(fileName);
        user.authorizeUser();
        userRepository.save(user);

        return newUserTag;
    }

    public UserRegisterResponse userRegisterBody(String userTag, Long userId
        , String userImageUrl) {

        return UserRegisterResponse.builder()
                    .userId(userId)
                    .userTag(userTag)
                    .imageUrl(userImageUrl)
                    .build();
    }

    // userTag 가 제대로 된 userTag 인지 검증하는 함수
    public boolean userTagValidation(String userTag) {
        if (userTag == null) {
            return false;
        }

        Pattern pattern = Pattern.compile("^[a-zA-Z\\uAC00-\\uD7A3\\d]+#\\d+$");
        Matcher matcher = pattern.matcher(userTag);
        return matcher.matches();
    }

    // userTag로 검색 후 user정보 반환
    public UserInfoResponse findUserTag(UserTagRequest userTagRequest, Long userId) {
        String userTag = userTagRequest.getUserTag();

        if (userTagValidation(userTag)) {
            User user = userRepository.findByUserTag(userTag)
                    .orElseThrow(() -> new ApplicationException(ApplicationError.USER_TAG_NOT_FOUND));
            boolean isRelationship = checkFriendship(userId, user.getId());
            boolean isFriend = false;
            if (isRelationship) {
                isFriend = friendshipRepository.findIsAcceptFriendshipsByUserId(userId, user.getId());
            }

            return UserInfoResponse.builder()
                    .userId(user.getId())
                    .nickname(user.getNickname())
                    .userTag(user.getUserTag())
                    .imageUrl(user.getImageUrl())
                    .isFriend(isFriend)
                    .isRelationship(isRelationship)
                    .build();
        } else {
            throw new ApplicationException(ApplicationError.INVALID_USER_TAG_FORMAT);
        }
    }

    // userTag 검색 후 해당 유저와의 상태를 나타내는 함수
    public boolean checkFriendship(Long userId, Long friendId) {
        return friendshipRepository.existsByUserIdAndFriendId(userId, friendId);
    }

    // 환경 설정에서 사용자의 기본 정보 불러오는 함수
    public UserSettingInformationResponse getUserInformation(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.USER_TAG_NOT_FOUND));

        return UserSettingInformationResponse.builder()
                .userTag(user.getUserTag())
                .nickname(user.getNickname())
                .imageUrl(user.getImageUrl())
                .socialType(user.getSocialType())
                .build();
    }

    // 닉네임 변경
    public UpdateNicknameResponse updateUserNickname(String token, UserNicknameRequest userNicknameRequest) {

        Long userId = jwtService.getLongUserIdFromToken(token);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.USER_NOT_FOUND));

        returnToNicknameCounter(user);

        String newNickname = userNicknameRequest.getNickname();
        String newUserTag = nickNameService.getUserTagFromNickName(newNickname);

        Map<String, String> tokenMap = jwtService.reissueToken(newUserTag);

        user.updateNickname(newNickname);
        user.updateUserTag(newUserTag);
        user.updateRefreshToken(tokenMap.get(REFRESHTOKEN));
        userRepository.save(user);

        return UpdateNicknameResponse.builder()
                .accessToken(tokenMap.get(ACCESSTOKEN))
                .refreshToken(user.getRefreshToken())
                .nickname(user.getNickname())
                .userTag(user.getUserTag())
                .imageUrl(user.getImageUrl())
                .socialType(user.getSocialType())
                .build();
    }

    // 이미지 변경
    public UserSettingInformationResponse updateUserImage(String token, MultipartFile file) throws IOException {

        Long userId = jwtService.getLongUserIdFromToken(token);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.USER_NOT_FOUND));

//        String originalFileName = user.getImageUrl();
//        awsS3Uploader.deleteS3(originalFileName);

        String newFileName = awsS3Uploader.newUpload(file,
                S3_USER_PROFILE_BASE_PATH + user.getId() + S3_USER_PROFILE_LAST_PATH);

        user.updateImageUrl(newFileName);
        userRepository.save(user);

        return UserSettingInformationResponse.builder()
                .nickname(user.getNickname())
                .userTag(user.getUserTag())
                .imageUrl(user.getImageUrl())
                .socialType(user.getSocialType())
                .build();
    }

    // 사용자 탈퇴
    public void deleteUser(String token) {

        Long userId = jwtService.getLongUserIdFromToken(token);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.USER_NOT_FOUND));

//        returnToNicknameCounter(user);

        if (user.getSocialType() == SocialType.KAKAO) {
            unlinkKakao(token);
        } else if (user.getSocialType() == SocialType.GOOGLE) {
            unlinkGoogle(token);
        }

        List<Friendship> friendshipList = friendshipRepository.findByUserIdOrFriendId(userId);
        for (Friendship friendship : friendshipList) {
            friendship.setDeleted();
            friendshipRepository.save(friendship);
        }

//        String originalFileName = user.getImageUrl();
//        awsS3Uploader.deleteS3(originalFileName);
        user.setDeleted();
        userRepository.save(user);
//        userRepository.deleteById(userId);
    }

    // nickname counter AvailableNumber에 태그 번호 반납
    public void returnToNicknameCounter(User user)  {
        String originalNickname = user.getNickname();
        String originalUserTag = user.getUserTag();
        String[] splitedString = originalUserTag.split("#");
        Integer originalTagNumber = Integer.parseInt(splitedString[1]);

        Optional<NicknameCounter> optionalNicknameCounter = nickNameRepository.findByNickname(originalNickname);

        if (optionalNicknameCounter.isPresent()) {
            NicknameCounter existingNicknameCounter = optionalNicknameCounter.get();
            existingNicknameCounter.addAvailableNumber(originalTagNumber);
        }
    }

    // KAKAO unlink
    public void unlinkKakao(String token) {
        String requestURI = "https://kapi.kakao.com/v1/unlink";

        try {
            URL url = new URL(requestURI);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + token);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    // GOOGLE unlink
    public void unlinkGoogle(String token) {
        String requestURI = "https://oauth2.googleapis.com/revoke?token=";

        try {
            URL url = new URL(requestURI + token);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
