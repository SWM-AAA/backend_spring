package kr.co.zeppy.chat.controller;

import kr.co.zeppy.ApiDocument;
import kr.co.zeppy.SecurityConfigTest;
import kr.co.zeppy.chat.dto.ChatRoomCreateRequest;
import kr.co.zeppy.chat.dto.ChatRoomResponse;
import kr.co.zeppy.chat.service.ChatRoomService;
import kr.co.zeppy.global.dto.ApiResponse;
import kr.co.zeppy.global.dto.ErrorResponse;
import kr.co.zeppy.global.error.ApplicationError;
import kr.co.zeppy.global.error.ApplicationException;
import kr.co.zeppy.global.jwt.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.ResultActions;

import java.util.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WithMockUser(username = "test", roles = "USER")
@WebMvcTest(ChatRoomController.class)
@Import(SecurityConfigTest.class)
class ChatRoomControllerTest extends ApiDocument {
    private static final String CHAT_ROOM_NAME = "testChatroom";
    private static final String API_VERSION = "/api/v1";
    private static final String RESOURCE_PATH = "/chatRoom";
    private static final String AUTHORIZATION_HEADER= "Authorization";
    private static final String ACCESSTOKEN = "accessToken";

    @MockBean
    private ChatRoomService chatRoomService;
    @MockBean
    private JwtService jwtService;

    private ChatRoomCreateRequest chatRoomCreateRequest;

    private ApplicationException userIdNotFoundException;
    private ChatRoomResponse chatRoomResponse;

    @BeforeEach
    void setUp() {
        userIdNotFoundException = new ApplicationException(ApplicationError.USER_NOT_FOUND);
        chatRoomCreateRequest = ChatRoomCreateRequest.builder()
                .userIdList(new ArrayList<>(Arrays.asList(1L, 2L, 3L)))
                .build();
        chatRoomResponse = ChatRoomResponse.builder()
                .id(1L)
                .name(CHAT_ROOM_NAME)
                .build();

    }

    /////////////////////////////////////////////////////////////////
    // ChatRoomController createChatRoom test code
    /////////////////////////////////////////////////////////////////
    @Test
    void test_Create_Chat_Room_Success() throws Exception {
        // given
        when(chatRoomService.generateRoomName()).thenReturn(CHAT_ROOM_NAME);
        when(chatRoomService.createChatRoom(anyList())).thenReturn(chatRoomResponse);

        // when
        ResultActions resultActions = Create_Chat_Room_Request(chatRoomCreateRequest);

        // then
        Create_Chat_Room_Request_Success(resultActions);
    }

    @Test
    void test_Create_Chat_Room_Failure() throws Exception {
        // given
        willThrow(userIdNotFoundException).given(chatRoomService).createChatRoom(anyList());

        // when
        ResultActions resultActions = Create_Chat_Room_Request(chatRoomCreateRequest);

        // then
        Create_Chat_Room_Request_Failure(resultActions);
    }

    private ResultActions Create_Chat_Room_Request(ChatRoomCreateRequest request) throws Exception {
        return mockMvc.perform(
                RestDocumentationRequestBuilders.post(API_VERSION + RESOURCE_PATH)
                        .header(AUTHORIZATION_HEADER, "Bearer " + ACCESSTOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request))
        );
    }

    private void Create_Chat_Room_Request_Success(ResultActions resultActions) throws Exception {
        printAndMakeSnippet(resultActions.andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(chatRoomResponse.getId()))
                    .andExpect(jsonPath("$.data.name").value(chatRoomResponse.getName())),
                "create-Chat-Room-Request-Success");
        verify(chatRoomService, times(1)).createChatRoom(anyList());
    }

    private void Create_Chat_Room_Request_Failure(ResultActions resultActions) throws Exception {
        printAndMakeSnippet(resultActions.andExpect(status().isNotFound()).andExpect(content()
                .json(toJson(ApiResponse.failure(ErrorResponse.fromException(userIdNotFoundException)))))
        ,"create-Chat-Room-Request-Failure");
        verify(chatRoomService, times(1)).createChatRoom(anyList());
    }
}