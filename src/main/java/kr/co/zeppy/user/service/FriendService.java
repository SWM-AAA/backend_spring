package kr.co.zeppy.user.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import kr.co.zeppy.global.error.ApplicationError;
import kr.co.zeppy.global.error.ApplicationException;
import kr.co.zeppy.global.jwt.service.JwtService;
import kr.co.zeppy.user.dto.ConfirmFriendshipRequest;
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
        
        user.addSentFriendships(friendship);
        friend.addReceivedFriendships(friendship);
        
        friendshipRepository.save(friendship);
    }

    // 나에게 친구추가 요청을 보낸 사용자 리스트를 확인
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

    // 내가 친구추가를 보낸 사용자 리스트를 확인
    public List<UserFriendInfoResponse> checkSentFriendRequestToList(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.USER_ID_NOT_FOUND));
        
        Set<Friendship> sentFriendRequests = user.getSentFriendships();

        List<UserFriendInfoResponse> friendRequestList = new ArrayList<>();

        for (Friendship request : sentFriendRequests) {
            if (request.getStatus() == FriendshipStatus.PENDING) {
                User reseiver = request.getFriend();
                friendRequestList.add(UserFriendInfoResponse.from(reseiver));
            }
        }

        return friendRequestList;
    }

    // 친구 추가 요청을 수락 및 거절
    public void confirmFriendship(Long userId, ConfirmFriendshipRequest confirmFriendshipRequest) {
        Long friendId = confirmFriendshipRequest.getUserId();
        boolean isAccept = confirmFriendshipRequest.isAccept();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.USER_ID_NOT_FOUND));
        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.USER_ID_NOT_FOUND));
        Friendship friendship = friendshipRepository.findByUserIdAndFriendId(friendId, userId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.FRIENDSHIP_NOT_FOUND));
        
        if (friendship.getStatus().equals(FriendshipStatus.ACCEPTED)) {
            throw new ApplicationException(ApplicationError.FRIENDSHIP_ALREADY_ACCEPTED);
        } else if (friendship.getStatus().equals(FriendshipStatus.DECLINE)) {
            throw new ApplicationException(ApplicationError.FRIENDSHIP_ALREADY_DECLINE);
        }

        if (isAccept) {
            acceptFriendship(friendship);
        } else {
            declineFriendship(friendship);
        }
        friend.removeSentFriendships(friendship);
        user.removeReceivedFriendships(friendship);

        friendshipRepository.save(friendship);
        // 알림 기능 완성 되면 알림
    }

    // 친구 추가 요청을 수락
    public void acceptFriendship(Friendship friendship) {
        friendship.acceptRequest();
    }

    // 친구 추가 요청을 거절
    public void declineFriendship(Friendship friendship) {
        friendship.declineRequest();
    }
    
    // user의 친구 리스트 반환
    public List<UserFriendInfoResponse> giveUserFriendList(Long userId) {
        List<UserFriendInfoResponse> userFriendList = new ArrayList<>();

        List<Friendship> friendships = friendshipRepository.findAllFriendshipsByUserId(userId);

        for (Friendship friendship : friendships) {
            User friend;
            if (friendship.getUser().getId().equals(userId)) {
                friend = friendship.getFriend();
            } else {
                friend = friendship.getUser();
            }
            userFriendList.add(UserFriendInfoResponse.from(friend));
        }

        return userFriendList;
    }
}
