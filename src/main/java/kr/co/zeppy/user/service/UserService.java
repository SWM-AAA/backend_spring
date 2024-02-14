package kr.co.zeppy.user.service;

import jakarta.transaction.Transactional;
import kr.co.zeppy.global.aws.service.AwsS3Uploader;
import kr.co.zeppy.global.error.ApplicationError;
import kr.co.zeppy.global.error.ApplicationException;
import kr.co.zeppy.global.jwt.service.JwtService;
import kr.co.zeppy.user.dto.*;
import kr.co.zeppy.user.entity.Role;
import kr.co.zeppy.user.entity.SocialType;
import kr.co.zeppy.user.entity.User;
import kr.co.zeppy.user.repository.FriendshipRepository;
import kr.co.zeppy.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
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
    private static final String USERTAG = "userTag";
    private static final String USERID = "userId";
    private static final String IMAGEURL = "imageUrl";

    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final JwtService jwtService;
    private final AwsS3Uploader awsS3Uploader;
    private final NickNameService nickNameService;
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
            .build();

        userRepository.save(user);
    }

    public void registerByUsername(UserRegisterByUsernameRequest userRegisterByUsernameRequest) throws Exception {

        if (userRepository.findByUsername(userRegisterByUsernameRequest.getUsername()).isPresent()) {
            throw new Exception("이미 존재하는 아이디입니다.");
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
                .build();

        user.passwordEncode(passwordEncoder);
        userRepository.save(user);
    }

    public String register(String Token, UserRegisterRequest userRegisterRequest)
            throws IOException {
        String userTag = jwtService.extractUserTagFromToken(Token)
                .orElseThrow(() -> new ApplicationException(ApplicationError.USER_TAG_NOT_FOUND));

        User user = userRepository.findByUserTag(userTag)
                .orElseThrow(() -> new ApplicationException(ApplicationError.USER_NOT_FOUND));

        MultipartFile file = userRegisterRequest.getProfileimage();
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

    public UserRegisterResponse userRegisterBody(String accessToken, String userTag, Long userId
        , String userImageUrl) {

        return UserRegisterResponse.builder()
                    .userId(userId)
                    .userTag(userTag)
                    .accessToken(accessToken)
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
}
