package kr.co.zeppy.location.service;

import kr.co.zeppy.location.dto.CurrentLocationModeResponse;
import kr.co.zeppy.location.dto.LocationModeTimerResponse;
import kr.co.zeppy.location.entity.LocationMode;
import kr.co.zeppy.location.entity.LocationModeStatus;
import kr.co.zeppy.location.repository.LocationModeRepository;
import kr.co.zeppy.user.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LocationModeServiceTest {

    @InjectMocks
    private LocationModeService locationModeService;

    @Mock
    private LocationModeRepository locationModeRepository;

    private static final String TOKEN = "sample_token";
    private static final Long INIT_USERID = 1L;
    private static final Long FRIENDID_2 = 2L;
    private static final Long FRIENDID_3 = 3L;
    private static final Long FRIENDID_4 = 4L;

    private static final String USER_NICKNAME = "UserNickname";
    private static final String USER_IMAGE_URL = "userImageUrl";
    private static final String USER_TAG = "User#0001";
    private static final Role USER_ROLE = Role.USER;
    private static final SocialType USER_SOCIAL_TYPE = SocialType.KAKAO;
    private static final String USER_SOCIAL_ID = "userSocialId";
    private static final String USER_REFRESH_TOKEN = "userRefreshToken";
    private static final String FRIEND_NICKNAME = "FriendNickname";
    private static final String FRIEND_IMAGE_URL = "friendImageUrl";
    private static final String FRIEND_TAG = "Friend#0001";
    private static final String FRIEND_TAG2 = "Friend#0002";
    private static final String FRIEND_TAG3 = "Friend#0003";
    private static final Role FRIEND_ROLE = Role.USER;
    private static final SocialType FRIEND_SOCIAL_TYPE = SocialType.GOOGLE;
    private static final String FRIEND_SOCIAL_ID = "friendSocialId";
    private static final String FRIEND_REFRESH_TOKEN = "friendRefreshToken";
    private static final Long LOCATION_MODE_ID_1 = 1L;
    private static final Long LOCATION_MODE_ID_2 = 2L;
    private static final Long LOCATION_MODE_ID_3 = 3L;

    private User user;
    private User friend;
    private User friend_2;
    private User friend_3;

    private LocationMode locationMode_1;
    private LocationMode locationMode_2;
    private LocationMode locationMode_3;
    private List<LocationMode> locationModeList;

    @BeforeEach
    public void setup() {
        user = User.builder()
                .id(INIT_USERID)
                .nickname(USER_NICKNAME)
                .imageUrl(USER_IMAGE_URL)
                .userTag(USER_TAG)
                .role(USER_ROLE)
                .socialType(USER_SOCIAL_TYPE)
                .socialId(USER_SOCIAL_ID)
                .refreshToken(USER_REFRESH_TOKEN)
                .activated(true)
                .build();

        friend = User.builder()
                .id(FRIENDID_2)
                .nickname(FRIEND_NICKNAME)
                .imageUrl(FRIEND_IMAGE_URL)
                .userTag(FRIEND_TAG)
                .role(FRIEND_ROLE)
                .socialType(FRIEND_SOCIAL_TYPE)
                .socialId(FRIEND_SOCIAL_ID)
                .refreshToken(FRIEND_REFRESH_TOKEN)
                .activated(true)
                .build();

        friend_2 = User.builder()
                .id(FRIENDID_3) // 3
                .nickname(FRIEND_NICKNAME)
                .imageUrl(FRIEND_IMAGE_URL)
                .userTag(FRIEND_TAG2)
                .role(FRIEND_ROLE)
                .socialType(FRIEND_SOCIAL_TYPE)
                .socialId(FRIEND_SOCIAL_ID)
                .refreshToken(FRIEND_REFRESH_TOKEN)
                .activated(true)
                .build();

        friend_3 = User.builder()
                .id(FRIENDID_4) // 4
                .nickname(FRIEND_NICKNAME)
                .imageUrl(FRIEND_IMAGE_URL)
                .userTag(FRIEND_TAG3)
                .role(FRIEND_ROLE)
                .socialType(FRIEND_SOCIAL_TYPE)
                .socialId(FRIEND_SOCIAL_ID)
                .refreshToken(FRIEND_REFRESH_TOKEN)
                .activated(true)
                .build();

        locationMode_1 = LocationMode.builder()
                .id(LOCATION_MODE_ID_1)
                .user(user)
                .friend(friend)
                .status(LocationModeStatus.ACCURATE)
                .build();

        locationMode_2 = LocationMode.builder()
                .id(LOCATION_MODE_ID_2)
                .user(user)
                .friend(friend_2)
                .status(LocationModeStatus.AMBIGUOUS)
                .build();

        locationMode_3 = LocationMode.builder()
                .id(LOCATION_MODE_ID_3)
                .user(user)
                .friend(friend_3)
                .status(LocationModeStatus.PINNED)
                .build();

        locationModeList = Arrays.asList(locationMode_1, locationMode_2, locationMode_3);
    }

    @Test
    void getTimes() {
        // Given

        // When
        LocationModeTimerResponse response = locationModeService.getTimes();

        // Then
        assertAll(
                () -> assertEquals(response.getTimes().get(0), 2),
                () -> assertEquals(response.getTimes().get(1), 4),
                () -> assertEquals(response.getTimes().get(2), 8),
                () -> assertEquals(response.getTimes().get(3), 24)
        );
    }

    @Test
    void getCurrentLocationMods() {
        // Given
        when(locationModeRepository.findByUserId(anyLong())).thenReturn(locationModeList);

        // When
        CurrentLocationModeResponse response = locationModeService.getLocationMode(1L);

        // Then
        assertAll(
                () -> assertEquals(1, response.getAccurate().size()),
                () -> assertEquals(1, response.getAmbiguous().size()),
                () -> assertEquals(1, response.getPinned().size()),
                () -> assertEquals(FRIENDID_2, response.getAccurate().get(0).getUserId()),
                () -> assertEquals(FRIENDID_3, response.getAmbiguous().get(0).getUserId()),
                () -> assertEquals(FRIENDID_4, response.getPinned().get(0).getUserId())
        );
    }
}
