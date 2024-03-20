package kr.co.zeppy.location.service;

import jakarta.transaction.Transactional;
import kr.co.zeppy.global.error.ApplicationError;
import kr.co.zeppy.global.error.ApplicationException;
import kr.co.zeppy.global.redis.service.RedisService;
import kr.co.zeppy.location.dto.CurrentLocationModeResponse;
import kr.co.zeppy.location.dto.FriendInfo;
import kr.co.zeppy.location.dto.LocationModeTimerResponse;
import kr.co.zeppy.location.dto.UpdateLocationModeRequest;
import kr.co.zeppy.location.entity.LocationMode;
import kr.co.zeppy.location.entity.LocationModeStatus;
import kr.co.zeppy.location.repository.LocationModeRepository;
import kr.co.zeppy.user.entity.User;
import kr.co.zeppy.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class LocationModeService {

    private final LocationModeRepository locationModeRepository;
    private final UserRepository userRepository;
    private final RedisService redisService;

    public LocationModeTimerResponse getTimes() {

        List<Integer> timeList = Arrays.asList(2, 4, 8, 24);
        return LocationModeTimerResponse.builder()
                .times(timeList)
                .build();
    }

    public CurrentLocationModeResponse getLocationMode(Long userId) {

        List<FriendInfo> accurateFriends = new ArrayList<>();
        List<FriendInfo> ambiguousFriends = new ArrayList<>();
        List<FriendInfo> pinnedFriends = new ArrayList<>();

        List<LocationMode> locationModes = locationModeRepository.findByUserId(userId);

        for (LocationMode l : locationModes) {
            FriendInfo friendInfo = FriendInfo.builder()
                    .userId(l.getFriend().getId())
                    .userTag(l.getFriend().getUserTag())
                    .imageUrl(l.getFriend().getImageUrl())
                    .build();

            switch (l.getStatus()) {
                case ACCURATE -> accurateFriends.add(friendInfo);
                case AMBIGUOUS -> ambiguousFriends.add(friendInfo);
                case PINNED -> pinnedFriends.add(friendInfo);
                default -> throw new ApplicationException(ApplicationError.LOCATION_MODE_NOT_FOUND);
            }
        }

        return CurrentLocationModeResponse.builder()
                .accurate(accurateFriends)
                .ambiguous(ambiguousFriends)
                .pinned(pinnedFriends)
                .build();
    }

    public CurrentLocationModeResponse updateMode(Long userId, UpdateLocationModeRequest updateLocationModeRequest) {

        List<Long> accurateFriends = updateLocationModeRequest.getAccurate();
        List<Long> ambiguousFriends = updateLocationModeRequest.getAmbiguous();
        List<Long> pinnedFriends = updateLocationModeRequest.getPinned();

        setFriends(userId, accurateFriends, LocationModeStatus.ACCURATE);
        setFriends(userId, ambiguousFriends, LocationModeStatus.AMBIGUOUS);
        setFriends(userId, pinnedFriends, LocationModeStatus.PINNED);

        return getLocationMode(userId);
    }

    // 친구 요청 수락 시 default accurate 모드
    public void setAccurateFriend(Long userId, Long friendId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.USER_NOT_FOUND));

        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.USER_NOT_FOUND));

        LocationMode userLocationMode = LocationMode.builder()
                .user(user)
                .friend(friend)
                .status(LocationModeStatus.ACCURATE)
                .build();

        LocationMode friendLocationMode = LocationMode.builder()
                .user(friend)
                .friend(user)
                .status(LocationModeStatus.ACCURATE)
                .build();

        locationModeRepository.save(userLocationMode);
        locationModeRepository.save(friendLocationMode);
    }

    public void setFriends(Long userId, List<Long> friendIdList, LocationModeStatus status) {

        for (Long friendId : friendIdList) {

            LocationMode locationMode = locationModeRepository.findByUserIdAndFriendId(userId, friendId)
                    .orElseThrow(() -> new ApplicationException(ApplicationError.LOCATION_MODE_NOT_FOUND));

            if (!locationMode.getStatus().equals(status)) {

                locationMode.changeLocationMode(status);
                locationModeRepository.save(locationMode);
            }
        }
    }
}
