package kr.co.zeppy.location.service;

import jakarta.transaction.Transactional;
import kr.co.zeppy.global.annotation.UserId;
import kr.co.zeppy.global.error.ApplicationError;
import kr.co.zeppy.global.error.ApplicationException;
import kr.co.zeppy.location.dto.CurrentLocationModeResponse;
import kr.co.zeppy.location.dto.FriendInfo;
import kr.co.zeppy.location.dto.LocationModeTimerResponse;
import kr.co.zeppy.location.dto.UpdateLocationModeRequest;
import kr.co.zeppy.location.entity.LocationMode;
import kr.co.zeppy.location.entity.LocationModeStatus;
import kr.co.zeppy.location.repository.LocationModeRepository;
import kr.co.zeppy.user.entity.User;
import kr.co.zeppy.user.repository.FriendshipRepository;
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

        List<LocationMode> accurateList = locationModeRepository.findAccurateFriendsByUserId(userId);
        List<LocationMode> ambiguousList = locationModeRepository.findAmbiguousFriendsByUserId(userId);
        List<LocationMode> pinnedList = locationModeRepository.findPinnedFriendsByUserId(userId);

        for (LocationMode l : accurateList) {
            FriendInfo friendInfo = FriendInfo.builder()
                    .userId(l.getFriend().getId())
                    .userTag(l.getFriend().getUserTag())
                    .imageUrl(l.getFriend().getImageUrl())
                    .build();
            accurateFriends.add(friendInfo);
        }

        for (LocationMode l : ambiguousList) {
            FriendInfo friendInfo = FriendInfo.builder()
                    .userId(l.getFriend().getId())
                    .userTag(l.getFriend().getUserTag())
                    .imageUrl(l.getFriend().getImageUrl())
                    .build();
            ambiguousFriends.add(friendInfo);
        }

        for (LocationMode l : pinnedList) {
            FriendInfo friendInfo = FriendInfo.builder()
                    .userId(l.getFriend().getId())
                    .userTag(l.getFriend().getUserTag())
                    .imageUrl(l.getFriend().getImageUrl())
                    .build();
            pinnedFriends.add(friendInfo);
        }

        return CurrentLocationModeResponse.builder()
                .accurate(accurateFriends)
                .ambiguous(ambiguousFriends)
                .pinned(pinnedFriends)
                .build();
    }

    public CurrentLocationModeResponse updateMode(Long userId, UpdateLocationModeRequest updateLocationModeRequest) {
        List<User> accurateFriends = updateLocationModeRequest.getAccurateFriends();
        List<User> ambiguousFriends = updateLocationModeRequest.getAccurateFriends();
        List<User> pinnedFriends = updateLocationModeRequest.getAccurateFriends();

        setFriends(userId, accurateFriends, LocationModeStatus.ACCURATE);
        setFriends(userId, ambiguousFriends, LocationModeStatus.AMBIGUOUS);
        setFriends(userId, pinnedFriends, LocationModeStatus.PINNED);

        return getLocationMode(userId);
    }

    // 친구 요청 수락 시
    public void setAccurateFriend(User user, User friend) {

        LocationMode locationMode = LocationMode.builder()
                .user(user)
                .friend(friend)
                .status(LocationModeStatus.ACCURATE)
                .build();

        // 내가 지정한 친구의 모드
        locationModeRepository.save(locationMode);

        // 친구가 지정한 나의 모드
        friend.addAccurateFriends(locationMode);
    }

    public void setFriends(Long userId, List<User> friends, LocationModeStatus status) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.USER_NOT_FOUND));

        for (User friend : friends) {
            LocationMode locationMode = LocationMode.builder()
                    .user(user)
                    .friend(friend)
                    .status(status)
                    .build();
            locationModeRepository.save(locationMode);

            LocationMode existingLocationMode = locationModeRepository.findByUserIdAndFriendId(user.getId(), friend.getId())
                    .orElseThrow(() -> new ApplicationException(ApplicationError.LOCATION_MODE_NOT_FOUND));

            switch(existingLocationMode.getStatus()) {
                case ACCURATE:
                    friend.removeAccurateFriends(existingLocationMode);
                    break;
                case AMBIGUOUS:
                    friend.removeAmbiguousFriends(existingLocationMode);
                    break;
                default:
                    friend.removePinnedFriends(existingLocationMode);
            }

            switch(status) {
                case ACCURATE:
                    friend.addAccurateFriends(locationMode);
                    break;
                case AMBIGUOUS:
                    friend.addAmbiguousFriends(locationMode);
                    break;
                default:
                    friend.addPinnedFriends(locationMode);
            }
        }
    }
}
