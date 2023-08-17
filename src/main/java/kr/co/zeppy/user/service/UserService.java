package kr.co.zeppy.user.service;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.transaction.Transactional;
import kr.co.zeppy.global.aws.service.AwsS3Uploader;
import kr.co.zeppy.global.error.ApplicationError;
import kr.co.zeppy.global.error.ApplicationException;
import kr.co.zeppy.global.jwt.service.JwtService;
import kr.co.zeppy.user.dto.UserRegisterRequest;
import kr.co.zeppy.user.dto.UserRegisterRequestTest;
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
    private final FriendshipRepository friendshipRepository;
    private final AwsS3Uploader awsS3Uploader;

    public void register(String accessToken, UserRegisterRequest userRegisterRequest) {
        String userTag = jwtService.extractUserTag(accessToken)
                .orElseThrow(() -> new ApplicationException(ApplicationError.USER_TAG_NOT_FOUND));

        User user = userRepository.findByUserTag(userTag)
                .orElseThrow(() -> new ApplicationException(ApplicationError.USER_NOT_FOUND));
        user.updateNickname(userRegisterRequest.getNickname());
        user.updateImageUrl(userRegisterRequest.getImageUrl());
    }


    public void registerTest(String accessToken, String temp, UserRegisterRequestTest userRegisterRequestTest) 
            throws IOException {
        // String userTag = jwtService.extractUserTag(accessToken)
        //         .orElseThrow(() -> new ApplicationException(ApplicationError.USER_TAG_NOT_FOUND));

        User user = userRepository.findByUserTag(temp)
                .orElseThrow(() -> new ApplicationException(ApplicationError.USER_NOT_FOUND));

        MultipartFile file = userRegisterRequestTest.getProfileimage();

        String fileName = awsS3Uploader.upload(file
                ,S3_USER_PROFILE_PATH + user.getId());
        
        user.updateNickname(userRegisterRequestTest.getNickname());
        user.updateImageUrl(fileName);
    }

    
    public String getUserIdFromToken(String token) {
        return jwtService.extractUserTagFromToken(token)
            .flatMap(userRepository::findIdByUserTag)
            .orElseThrow(() -> new ApplicationException(ApplicationError.USER_ID_NOT_FOUND))
            .toString();
    }


    @Transactional
    public void sendFriendRequest(Long userId, Long friendId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ApplicationException(ApplicationError.USER_NOT_FOUND));

        User friend = userRepository.findById(friendId)
            .orElseThrow(() -> new ApplicationException(ApplicationError.USER_NOT_FOUND));
        
        Friendship friendship = Friendship.builder()
            .user(user)
            .friend(friend)
            .status(FriendshipStatus.PENDING)
            .build();
        
        friendshipRepository.save(friendship);
    }

    @Transactional
    public void acceptFriendRequest(Long userId, Long friendId) {
        Friendship friendship = friendshipRepository.findByUserIdAndFriendId(userId, friendId)
            .orElseThrow(() -> new ApplicationException(ApplicationError.FRIEND_REQUEST_NOT_FOUND));
        
        friendship.acceptRequest();
    }

    @Transactional
    public void declineFriendRequest(Long userId, Long friendId) {
        Friendship friendship = friendshipRepository.findByUserIdAndFriendId(userId, friendId)
            .orElseThrow(() -> new ApplicationException(ApplicationError.FRIEND_REQUEST_NOT_FOUND));
        
        friendshipRepository.delete(friendship);
    }
}
