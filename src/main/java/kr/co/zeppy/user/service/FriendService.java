package kr.co.zeppy.user.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import kr.co.zeppy.global.error.ApplicationError;
import kr.co.zeppy.global.error.ApplicationException;
import kr.co.zeppy.global.jwt.service.JwtService;
import kr.co.zeppy.user.dto.FriendshipRequest;
import kr.co.zeppy.user.entity.Friendship;
import kr.co.zeppy.user.entity.FriendshipStatus;
import kr.co.zeppy.user.entity.User;
import kr.co.zeppy.user.repository.FriendshipRepository;
import kr.co.zeppy.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import kr.co.zeppy.user.dto.UserFriendInfoResponse;

@Service
@Transactional
@RequiredArgsConstructor
public class FriendService {

    private final JwtService jwtService;
    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    // user 친구 추가 기능
    // test code 미작성
    public void sendFriendRequest(String token, FriendshipRequest friendshipRequest) {
        Long userId = jwtService.getLongUserIdFromToken(token);
        Long friendId = friendshipRequest.getUserId();

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


    public List<UserFriendInfoResponse> checkFriendRequestToList(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.USER_NOT_FOUND));
        
        Set<Friendship> receivedFriendRequests = user.getReceivedFriendships();
    
        List<UserFriendInfoResponse> friendRequestList = new ArrayList<>();
    
        for (Friendship request : receivedFriendRequests) {
            if (request.getStatus() == FriendshipStatus.PENDING) {
                User requester = request.getUser(); // 요청을 보낸 사용자
                friendRequestList.add(UserFriendInfoResponse.from(requester));
            }
        }
    
        return friendRequestList;
    }
}
