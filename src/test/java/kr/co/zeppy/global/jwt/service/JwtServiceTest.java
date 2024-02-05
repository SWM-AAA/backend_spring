package kr.co.zeppy.global.jwt.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;

import com.auth0.jwt.JWT;

import kr.co.zeppy.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;


@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {

    private static final String ACCESS_TOKEN_SUBJECT = "accessToken";
    // private static final String REFRESH_TOKEN_SUBJECT = "refreshToken";
    private static final String LOGIN_USER_TAG = "userTag";
    private static final Long LOGIN_ID = 1L;

    @InjectMocks
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(userRepository);

        ReflectionTestUtils.setField(jwtService, "secretKey", "yourSecretKey");
        ReflectionTestUtils.setField(jwtService, "accessTokenExpirationPeriod", 3600L); // 예: 1시간
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpirationPeriod", 7200L); // 예: 2시간
        ReflectionTestUtils.setField(jwtService, "accessHeader", "Authorization");
        ReflectionTestUtils.setField(jwtService, "refreshHeader", "RefreshToken");
        ReflectionTestUtils.setField(jwtService, "accessTokenName", "access_token");
        ReflectionTestUtils.setField(jwtService, "refreshTokenName", "refresh_token");
    }

    @Test
    void test_Create_Access_Token() {
        // given
        String user_Login_id = LOGIN_ID.toString();

        //when
        String accessToken = jwtService.createAccessToken(user_Login_id);

        // then
        assertAll(
            () -> assertNotNull(accessToken),
            () -> assertThat(JWT.decode(accessToken).getSubject()).isEqualTo(ACCESS_TOKEN_SUBJECT),
            () -> assertThat(JWT.decode(accessToken).getClaim(LOGIN_USER_TAG).asString()).isEqualTo(user_Login_id),
            () -> {
                Date current = new Date();
                Date tokenExpiry = JWT.decode(accessToken).getIssuedAt();
                long differenceInMillis = tokenExpiry.getTime() - current.getTime();
                assertTrue(differenceInMillis <= 1000);
            }
        );
    }
}
    