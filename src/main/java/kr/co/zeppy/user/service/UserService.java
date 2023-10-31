package kr.co.zeppy.user.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import kr.co.zeppy.user.dto.UserTagRequest;
import kr.co.zeppy.user.entity.User;
import kr.co.zeppy.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private static final String S3_USER_PROFILE_PATH = "user/profile-image/";
    private static final String ACCESSTOKEN = "accessToken";
    private static final String USERTAG = "userTag";
    private static final String USERID = "userId";

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

    public Map<String, String> userRegisterBody(String accessToken, String userTag, String userId) {
        Map<String, String> responseBody = new HashMap<>();
        
        responseBody.put(ACCESSTOKEN, accessToken);
        responseBody.put(USERTAG, userTag);
        responseBody.put(USERID, userId);

        return responseBody;
    }

    // userTag 가 제대로 된 userTag 인지 검증하는 함수
    public boolean userTagValidation(String userTag) {
        Pattern pattern = Pattern.compile("^[a-zA-Z\uAC00-\uD7A3]+#\\\\d+$");
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
