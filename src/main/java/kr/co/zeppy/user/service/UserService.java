package kr.co.zeppy.user.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.transaction.Transactional;
import kr.co.zeppy.global.aws.service.AwsS3Uploader;
import kr.co.zeppy.global.error.ApplicationError;
import kr.co.zeppy.global.error.ApplicationException;
import kr.co.zeppy.global.jwt.service.JwtService;
import kr.co.zeppy.user.dto.UserInfoResponse;
import kr.co.zeppy.user.dto.UserPinInformationResponse;
import kr.co.zeppy.user.dto.UserRegisterRequest;
import kr.co.zeppy.user.dto.UserRegisterResponse;
import kr.co.zeppy.user.dto.UserTagRequest;
import kr.co.zeppy.user.entity.Role;
import kr.co.zeppy.user.entity.SocialType;
import kr.co.zeppy.user.entity.User;
import kr.co.zeppy.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private static final String S3_USER_PROFILE_PATH = "user/profile-image/";
    private static final String ACCESSTOKEN = "accessToken";
    private static final String USERTAG = "userTag";
    private static final String USERID = "userId";
    private static final String IMAGEURL = "imageUrl";

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AwsS3Uploader awsS3Uploader;
    private final NickNameService nickNameService;


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

    public String register(String Token, UserRegisterRequest userRegisterRequest) 
            throws IOException {
        String userTag = jwtService.extractUserTagFromToken(Token)
                .orElseThrow(() -> new ApplicationException(ApplicationError.USER_TAG_NOT_FOUND));

        User user = userRepository.findByUserTag(userTag)
                .orElseThrow(() -> new ApplicationException(ApplicationError.USER_NOT_FOUND));
        
        MultipartFile file = userRegisterRequest.getProfileimage();
        String newUserTag = nickNameService.getUserTagFromNickName(userRegisterRequest.getNickname());

        String fileName = awsS3Uploader.upload(file
                ,S3_USER_PROFILE_PATH + user.getId());
        
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
    
        Pattern pattern = Pattern.compile("^[a-zA-Z\\uAC00-\\uD7A3]+#\\d+$");
        Matcher matcher = pattern.matcher(userTag);
        return matcher.matches();
    }
    

    // userTag로 검색 후 user정보 반환
    public UserInfoResponse findUserTag(UserTagRequest userTagRequest) {
        String userTag = userTagRequest.getUserTag();
    
        if (userTagValidation(userTag)) {
            User user = userRepository.findByUserTag(userTag)
                    .orElseThrow(() -> new ApplicationException(ApplicationError.USER_TAG_NOT_FOUND));
    
            return UserInfoResponse.builder()
                    .userId(user.getId())
                    .nickname(user.getNickname())
                    .userTag(user.getUserTag())
                    .imageUrl(user.getImageUrl())
                    .build();
        } else {
            throw new ApplicationException(ApplicationError.INVALID_USER_TAG_FORMAT);
        }
    }

}
