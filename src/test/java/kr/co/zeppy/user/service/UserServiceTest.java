package kr.co.zeppy.user.service;

import com.auth0.jwt.JWT;
import kr.co.zeppy.global.aws.service.AwsS3Uploader;
import kr.co.zeppy.global.dto.ApiResponse;
import kr.co.zeppy.global.error.ApplicationError;
import kr.co.zeppy.global.error.ApplicationException;
import kr.co.zeppy.global.jwt.service.JwtService;
import kr.co.zeppy.user.dto.*;
import kr.co.zeppy.user.entity.*;
import kr.co.zeppy.user.repository.FriendshipRepository;
import kr.co.zeppy.user.repository.NickNameRepository;
import kr.co.zeppy.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private NickNameService nickNameService;

    @Mock
    private AwsS3Uploader awsS3Uploader;

    @Mock
    private NickNameRepository nickNameRepository;

    @Mock
    private NicknameCounter nicknameCounter;

    @Mock
    private FriendshipRepository friendshipRepository;

    private static final String S3_USER_PROFILE_BASE_PATH = "user/profile-image/";
    private static final String S3_USER_PROFILE_LAST_PATH = "profile";
    private static final String TOKEN = "testToken";

    private static final String USER_TAG = "testUserTag#0001";
    private static final String NEW_USER_TAG = "newUserTag";
    private static final String NICKNAME = "newNickname";
    private static final String USERNAME = "userName";
    private static final String PASSWORD = "password";

    private static final String NEW_NICKNAME = "changedNickname";
    private static final Long USER_ID = 1L;
    private static final String FILE_NAME = "testFileName";
    private static final String IMAGE_URL = "testImageUrl";
    private static final String PROFILE_IMAGE_NAME = "profileImage";
    private static final String FILE_TYPE = "image/jpeg";
    private static final byte[] IMAGE_CONTENT = "image content".getBytes();

    private static final Long INIT_USERID = 1L;
    private static final String USER_NICKNAME = "userNickname";
    private static final String USER_IMAGE_URL = "userImageUrl";
    private static final Role USER_ROLE = Role.USER;
    private static final SocialType USER_SOCIAL_TYPE = SocialType.KAKAO;
    private static final String USER_SOCIAL_ID = "userSocialId";
    private static final String USER_REFRESH_TOKEN = "userRefreshToken";


    private UserRegisterRequest userRegisterRequest;
    private UserRegisterByUsernameRequest userRegisterByUsernameRequest;
    private User user;
    private MultipartFile file;
    private List<Friendship> friendshipList;

    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setup(){
        file = new MockMultipartFile(PROFILE_IMAGE_NAME, FILE_NAME, FILE_TYPE, IMAGE_CONTENT);
        userRegisterRequest = UserRegisterRequest.builder()
                .nickname(NICKNAME)
                .profileImage(file)
                .build();
        userRegisterByUsernameRequest = UserRegisterByUsernameRequest.builder()
                .username(USERNAME)
                .password(PASSWORD)
                .build();

        user = mock(User.class);
//        when(user.getId()).thenReturn(USER_ID);
        friendshipList = new ArrayList<>();
    }

    @Test
    @Disabled
    void register() throws IOException {
        // Given
        when(jwtService.extractUserTagFromToken(TOKEN)).thenReturn(Optional.of(USER_TAG));

        when(userRepository.findByUserTag(USER_TAG)).thenReturn(Optional.of(user));
        when(nickNameService.getUserTagFromNickName(NICKNAME)).thenReturn(NEW_USER_TAG);
        when(awsS3Uploader.newUpload(file, S3_USER_PROFILE_BASE_PATH + USER_ID +
                S3_USER_PROFILE_LAST_PATH)).thenReturn(IMAGE_URL);

        // When
        userService.register(TOKEN, userRegisterRequest);

        // Then
        verify(user).updateUserTag(NEW_USER_TAG);
        verify(user).updateNickname(NICKNAME);
        verify(user).updateImageUrl(IMAGE_URL);
    }

    @Test
    void registerByUsername() throws Exception {
        // Given
        user = User.builder()
                .id(INIT_USERID)
                .nickname(USER_NICKNAME)
                .imageUrl(USER_IMAGE_URL)
                .userTag(USER_TAG)
                .role(USER_ROLE)
                .socialType(USER_SOCIAL_TYPE)
                .socialId(USER_SOCIAL_ID)
                .refreshToken(USER_REFRESH_TOKEN)
                .build();

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(nickNameService.getUserTagFromNickName(anyString())).thenReturn(USER_TAG);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.createRefreshToken()).thenReturn(USER_REFRESH_TOKEN);

        // When
        UserRegisterByUsernameResponse response = userService.registerByUsername(userRegisterByUsernameRequest);

        // Then
        verify(userRepository).save(any(User.class));
        assertAll(
                () -> assertEquals(USERNAME, response.getUsername()),
                () -> assertEquals(USER_TAG, response.getUserTag()),
                () -> assertEquals(USER_REFRESH_TOKEN, response.getRefreshToken())
        );
    }

    @Test
    void getUserInformation() {
        // Given
        user = User.builder()
                .id(INIT_USERID)
                .nickname(USER_NICKNAME)
                .imageUrl(USER_IMAGE_URL)
                .userTag(USER_TAG)
                .role(USER_ROLE)
                .socialType(USER_SOCIAL_TYPE)
                .socialId(USER_SOCIAL_ID)
                .refreshToken(USER_REFRESH_TOKEN)
                .build();

        when(userRepository.findById(INIT_USERID)).thenReturn(Optional.of(user));

        // When
        UserSettingInformationResponse response = userService.getUserInformation(INIT_USERID);

        // Then
        assertEquals(user.getNickname(), response.getNickname());
        assertEquals(user.getUserTag(), response.getUserTag());
        assertEquals(user.getImageUrl(), response.getImageUrl());
        assertEquals(user.getSocialType(), response.getSocialType());
    }

    @Test
    void updateUserNickname() {
        // Given
        user = User.builder()
                .id(INIT_USERID)
                .nickname(USER_NICKNAME)
                .imageUrl(USER_IMAGE_URL)
                .userTag(USER_TAG)
                .role(USER_ROLE)
                .socialType(USER_SOCIAL_TYPE)
                .socialId(USER_SOCIAL_ID)
                .refreshToken(USER_REFRESH_TOKEN)
                .build();

        when(jwtService.getLongUserIdFromToken(TOKEN)).thenReturn(INIT_USERID);
        when(userRepository.findById(INIT_USERID)).thenReturn(Optional.of(user));
        when(nickNameRepository.findByNickname(USER_NICKNAME)).thenReturn(Optional.of(nicknameCounter));

        UserNicknameRequest request = UserNicknameRequest.builder()
                .nickname(NEW_NICKNAME)
                .build();

        // When
        userService.updateUserNickname(TOKEN, request);

        // Then
        assertEquals(NEW_NICKNAME, user.getNickname());
        verify(nicknameCounter).addAvailableNumber(1);
    }

    @Test
    void updateUserImage() throws IOException {
        // Given
        user = User.builder()
                .id(INIT_USERID)
                .nickname(USER_NICKNAME)
                .imageUrl(USER_IMAGE_URL)
                .userTag(USER_TAG)
                .role(USER_ROLE)
                .socialType(USER_SOCIAL_TYPE)
                .socialId(USER_SOCIAL_ID)
                .refreshToken(USER_REFRESH_TOKEN)
                .build();

        when(jwtService.getLongUserIdFromToken(TOKEN)).thenReturn(INIT_USERID);
        when(userRepository.findById(INIT_USERID)).thenReturn(Optional.of(user));
        when(awsS3Uploader.newUpload(file, S3_USER_PROFILE_BASE_PATH + USER_ID +
                S3_USER_PROFILE_LAST_PATH)).thenReturn(IMAGE_URL);

        // When
        userService.updateUserImage(TOKEN, file);

        // Then
        assertEquals(IMAGE_URL, user.getImageUrl());
    }

    @Test
    void deleteUser() throws IOException {
        // Given
        user = User.builder()
                .id(INIT_USERID)
                .nickname(USER_NICKNAME)
                .imageUrl(USER_IMAGE_URL)
                .userTag(USER_TAG)
                .role(USER_ROLE)
                .socialType(USER_SOCIAL_TYPE)
                .socialId(USER_SOCIAL_ID)
                .refreshToken(USER_REFRESH_TOKEN)
                .activated(Boolean.TRUE)
                .build();

        User user2 = User.builder()
                .id(2L)
                .nickname(USER_NICKNAME)
                .imageUrl(USER_IMAGE_URL)
                .userTag(USER_TAG)
                .role(USER_ROLE)
                .socialType(USER_SOCIAL_TYPE)
                .socialId(USER_SOCIAL_ID)
                .refreshToken(USER_REFRESH_TOKEN)
                .activated(Boolean.TRUE)
                .build();

        User user3 = User.builder()
                .id(3L)
                .nickname(USER_NICKNAME)
                .imageUrl(USER_IMAGE_URL)
                .userTag(USER_TAG)
                .role(USER_ROLE)
                .socialType(USER_SOCIAL_TYPE)
                .socialId(USER_SOCIAL_ID)
                .refreshToken(USER_REFRESH_TOKEN)
                .activated(Boolean.TRUE)
                .build();

        Friendship friendship1 = Friendship.builder()
                .id(1L)
                .user(user)
                .friend(user2)
                .status(FriendshipStatus.ACCEPTED)
                .deleted(Boolean.FALSE)
                .build();

        Friendship friendship2 = Friendship.builder()
                .id(2L)
                .user(user3)
                .friend(user)
                .status(FriendshipStatus.PENDING)
                .deleted(Boolean.FALSE)
                .build();

        friendshipList.add(friendship1);
        friendshipList.add(friendship2);

        when(jwtService.getLongUserIdFromToken(TOKEN)).thenReturn(INIT_USERID);
        when(userRepository.findById(INIT_USERID)).thenReturn(Optional.of(user));
        when(friendshipRepository.findByUserIdOrFriendId(INIT_USERID)).thenReturn(friendshipList);

        // When
        userService.deleteUser(TOKEN);

        // Then
//        verify(userRepository).deleteById(INIT_USERID);
        verify(userRepository).save(user);
        assertEquals(false, user.getActivated());
    }
}