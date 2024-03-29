package kr.co.zeppy.user.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import kr.co.zeppy.global.dto.ApiResponse;
import kr.co.zeppy.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import kr.co.zeppy.global.error.ApplicationError;
import kr.co.zeppy.global.error.ApplicationException;
import kr.co.zeppy.global.jwt.service.JwtService;
import kr.co.zeppy.user.dto.ConfirmFriendshipRequest;
import kr.co.zeppy.user.dto.DeleteFriendRequest;
import kr.co.zeppy.user.dto.FriendshipRequest;
import kr.co.zeppy.user.entity.Friendship;
import kr.co.zeppy.user.entity.FriendshipStatus;
import kr.co.zeppy.user.entity.User;
import kr.co.zeppy.user.repository.FriendshipRepository;
import lombok.RequiredArgsConstructor;
import kr.co.zeppy.user.dto.UserFriendInfoResponse;
import lombok.extern.slf4j.Slf4j;


@Slf4j
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

    // 친구 삭제
    public void deleteFriend(Long userId, DeleteFriendRequest deleteFriendRequest) {
        Long friendId = deleteFriendRequest.getFriendId();

        Optional<Friendship> friendship = friendshipRepository.
                findByUserIdAndFriendIdAndStatus(userId, friendId, FriendshipStatus.ACCEPTED);

        friendship.ifPresent(friendshipRepository::delete);
    }

    // 나에게 친구추가 요청을 보낸 사용자 리스트를 확인
    public List<UserFriendInfoResponse> checkFriendRequestToList(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.USER_NOT_FOUND));
        
        Set<Friendship> receivedFriendRequests = user.getReceivedFriendships();
    
        List<UserFriendInfoResponse> response = new ArrayList<>();
    
        for (Friendship request : receivedFriendRequests) {
            if (request.getStatus() == FriendshipStatus.PENDING) {
                User requester = request.getUser(); // 요청을 보낸 사용자
                response.add(UserFriendInfoResponse.from(requester));
            }
        }

        return response;
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

        friend.removeSentFriendships(friendship);
        user.removeReceivedFriendships(friendship);

        if (isAccept) {
            acceptFriendship(friendship);
            friendshipRepository.save(friendship);
        } else {
            declineFriendship(friendship);
            friendshipRepository.delete(friendship);
        }
        // todo : 지금은 friendship db를 삭제 나중에는 요청 쿨타임이라던지 다른 기능 보완 필요
        // todo : isAccept가 true 즉 수락했을때 friendship db에 없는 것 같음
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
