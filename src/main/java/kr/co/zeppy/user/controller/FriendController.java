package kr.co.zeppy.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nimbusds.openid.connect.sdk.rp.ApplicationType;

import kr.co.zeppy.global.annotation.UserId;
import kr.co.zeppy.global.error.ApplicationError;
import kr.co.zeppy.global.error.ApplicationException;
import kr.co.zeppy.user.dto.FriendshipRequest;
import kr.co.zeppy.user.entity.User;
import kr.co.zeppy.user.repository.UserRepository;
import kr.co.zeppy.user.service.FriendService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class FriendController {

    private final FriendService friendService;
    private final UserRepository userRepository;
    
    // 친구 추가 요청 post
    @PostMapping("/v1/friends/requests")
    public ResponseEntity<Void> sendFriendRequest(@RequestHeader("Authorization") String token,
            @RequestBody FriendshipRequest friendshipRequest) {
        
        friendService.sendFriendRequest(token, friendshipRequest);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/v1/annotation/test")
    public ResponseEntity<Long> annotationTest(@UserId Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.USER_ID_NOT_FOUND));

        return ResponseEntity.ok().body(user.getId());
    }
}
