package kr.co.zeppy.user.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Optional;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import kr.co.zeppy.global.aws.service.AwsS3Uploader;
import kr.co.zeppy.global.jwt.service.JwtService;
import kr.co.zeppy.user.dto.UserRegisterRequest;
import kr.co.zeppy.user.entity.User;
import kr.co.zeppy.user.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    private static final String S3_USER_PROFILE_PATH = "user/profile-image/";
    private static final String TOKEN = "testToken";
    private static final String USER_TAG = "testUserTag";
    private static final String NEW_USER_TAG = "newUserTag";
    private static final String NICKNAME = "newNickname";
    private static final Long USER_ID = 1L;
    private static final String FILE_NAME = "testFileName";
    private static final String IMAGE_URL = "testImageUrl";
    private static final String PROFILE_IMAGE_NAME = "profileimage";
    private static final String FILE_TYPE = "image/jpeg";
    private static final byte[] IMAGE_CONTENT = "image content".getBytes();

    private UserRegisterRequest userRegisterRequest;
    private User user;
    private MultipartFile file;

    @BeforeEach
    public void setup(){
        file = new MockMultipartFile(PROFILE_IMAGE_NAME, FILE_NAME, FILE_TYPE, IMAGE_CONTENT);
        userRegisterRequest = UserRegisterRequest.builder()
            .nickname(NICKNAME)
            .profileimage(file)
            .build();

        user = mock(User.class);
        when(user.getId()).thenReturn(USER_ID);
    }
        
    @Test
    void register() throws IOException {
        // Given
        when(jwtService.extractUserTagFromToken(TOKEN)).thenReturn(Optional.of(USER_TAG));
        when(userRepository.findByUserTag(USER_TAG)).thenReturn(Optional.of(user));
        when(nickNameService.getUserTagFromNickName(NICKNAME)).thenReturn(NEW_USER_TAG);
        when(awsS3Uploader.upload(file, S3_USER_PROFILE_PATH + USER_ID)).thenReturn(IMAGE_URL);

        // When
        userService.register(TOKEN, userRegisterRequest);

        // Then
        verify(user).updateUserTag(NEW_USER_TAG);
        verify(user).updateNickname(NICKNAME);
        verify(user).updateImageUrl(IMAGE_URL);
    }
}