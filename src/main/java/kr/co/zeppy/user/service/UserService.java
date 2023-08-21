package kr.co.zeppy.user.service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.transaction.Transactional;
import kr.co.zeppy.global.aws.service.AwsS3Uploader;
import kr.co.zeppy.global.error.ApplicationError;
import kr.co.zeppy.global.error.ApplicationException;
import kr.co.zeppy.global.jwt.service.JwtService;
import kr.co.zeppy.user.dto.UserPinInformationResponse;
import kr.co.zeppy.user.dto.UserRegisterRequest;
import kr.co.zeppy.user.dto.FriendshipRequest;
import kr.co.zeppy.user.dto.FriendshipResponse;
import kr.co.zeppy.user.entity.Friendship;
import kr.co.zeppy.user.entity.FriendshipStatus;
import kr.co.zeppy.user.entity.User;
import kr.co.zeppy.user.repository.FriendshipRepository;
import kr.co.zeppy.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private static final String S3_USER_PROFILE_PATH = "user/profile-image/";

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

        return newUserTag;
    }
}
