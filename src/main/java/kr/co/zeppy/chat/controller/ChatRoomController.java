package kr.co.zeppy.chat.controller;

import kr.co.zeppy.chat.dto.ChatRoomCreateRequest;
import kr.co.zeppy.chat.dto.ChatRoomResponse;
import kr.co.zeppy.chat.entity.ChatRoom;
import kr.co.zeppy.chat.service.ChatRoomService;
import kr.co.zeppy.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @PostMapping("/v1/chatRoom")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> createChatRoom(@RequestBody ChatRoomCreateRequest chatRoomCreateRequest) {
        ChatRoomResponse response = chatRoomService.createChatRoom(chatRoomCreateRequest.getUserIdList());

        return ResponseEntity.ok().body(ApiResponse.success(response));
    }

}