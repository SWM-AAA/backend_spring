package kr.co.zeppy.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kr.co.zeppy.user.dto.FriendshipRequest;
import kr.co.zeppy.user.service.FriendService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class FriendController {

    private final FriendService friendService;
    
        // 친구 추가 요청 post
    @PostMapping("/v1/friends/requests")
    public ResponseEntity<Void> sendFriendRequest(@RequestHeader("Authorization") String token,
            @RequestBody FriendshipRequest friendshipRequest) {
        
        friendService.sendFriendRequest(token, friendshipRequest);

        return ResponseEntity.ok().build();
    }
}
