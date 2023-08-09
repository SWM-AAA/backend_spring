package kr.co.zeppy.user.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.boot.test.context.SpringBootTest;

import kr.co.zeppy.user.entity.NicknameCounter;
import kr.co.zeppy.user.repository.NickNameRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

@SpringBootTest
public class NickNameServiceTest {

    private static final String USER_NICK_NAME = "userNickName";

    @InjectMocks
    private NickNameService nickNameService;

    @Mock
    private NickNameRepository nickNameRepository;

    @BeforeEach
    public void setup(){

    }
        
    @Test
    void get_User_Tag_To_NickName_Not_Exists() {
        // given
        String userTag = USER_NICK_NAME;

        //when
        String nickName = nickNameService.getUserTagToNickName(userTag);

        // then
        assertAll(
            () -> assertNotNull(nickName),
            () -> assertEquals(USER_NICK_NAME + "#0001", nickName)
        );
        
    }

    @Test
    void get_User_Tag_To_NickName_Exists() {
        // given
        String userTag = USER_NICK_NAME;
        NicknameCounter counter = NicknameCounter.builder()
                .nickname(userTag)
                .lastAssignedNumber(1)
                .build();
        
        //when
        nickNameRepository.save(counter);
        Mockito.when(nickNameRepository.findByNickname(userTag)).thenReturn(Optional.of(counter));
        String nickName = nickNameService.getUserTagToNickName(userTag);
    
        // then
        assertAll(
            () -> assertNotNull(nickName),
            () -> assertEquals(userTag + "#0002", nickName)
        );
    }
    

    @Test
    void get_User_Tag_To_NickName_Exists_And_Switch_Digit() {
        // given
        String userTag = USER_NICK_NAME;
        NicknameCounter counter = NicknameCounter.builder()
                .nickname(userTag)
                .lastAssignedNumber(9999)
                .build();

        //when
        nickNameRepository.save(counter);
        Mockito.when(nickNameRepository.findByNickname(userTag)).thenReturn(Optional.of(counter));
        String nickName = nickNameService.getUserTagToNickName(userTag);

        // then
        assertAll(
            () -> assertNotNull(nickName),
            () -> assertEquals(userTag + "#10000", nickName)
        );
    }
}
    