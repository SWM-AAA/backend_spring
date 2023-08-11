package kr.co.zeppy.user.service;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import kr.co.zeppy.global.error.ApplicationError;
import kr.co.zeppy.global.error.ApplicationException;
import kr.co.zeppy.global.jwt.service.JwtService;
import kr.co.zeppy.user.dto.UserRegisterRequest;
import kr.co.zeppy.user.entity.Friendship;
import kr.co.zeppy.user.entity.FriendshipStatus;
import kr.co.zeppy.user.entity.User;
import kr.co.zeppy.user.repository.FriendshipRepository;
import kr.co.zeppy.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final FriendshipRepository friendshipRepository;

    @Transactional
    public void register(String accessToken, UserRegisterRequest userRegisterRequest) {
        String userTag = jwtService.extractUserTag(accessToken)
                .orElseThrow(() -> new ApplicationException(ApplicationError.USER_TAG_NOT_FOUND));

        User user = userRepository.findByUserTag(userTag)
                .orElseThrow(() -> new ApplicationException(ApplicationError.USER_NOT_FOUND));
        user.updateNickname(userRegisterRequest.getNickname());
        user.updateImageUrl(userRegisterRequest.getImageUrl());
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
