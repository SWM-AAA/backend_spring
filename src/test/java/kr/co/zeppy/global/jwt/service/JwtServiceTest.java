package kr.co.zeppy.global.jwt.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import com.auth0.jwt.JWT;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import kr.co.zeppy.SecurityConfigTest;
import kr.co.zeppy.user.controller.UserController;

@SpringBootTest
public class JwtServiceTest {

    private static final String ACCESS_TOKEN_SUBJECT = "AccessToken";
    private static final String REFRESH_TOKEN_SUBJECT = "RefreshToken";
    private static final String LOGIN_USER_TAG = "userTag";
    private static final Long LOGIN_ID = 1L;

    @BeforeEach
    void setUp() {

    }

    @Autowired
    private JwtService jwtService;

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
    