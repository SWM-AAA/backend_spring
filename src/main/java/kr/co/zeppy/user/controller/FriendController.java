package kr.co.zeppy.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import kr.co.zeppy.user.dto.FriendshipRequest;

public class FriendController {
    
        // 친구 추가 요청 post
    @PostMapping("/v1/users/send-friend-request")
    public ResponseEntity<Void> sendFriendRequest(@RequestHeader("Authorization") String token,
            @RequestBody FriendshipRequest friendshipRequest) {

        return ResponseEntity.ok().build();
    }
}
