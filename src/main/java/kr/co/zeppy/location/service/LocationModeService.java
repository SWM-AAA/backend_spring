package kr.co.zeppy.location.service;

import jakarta.transaction.Transactional;
import kr.co.zeppy.global.annotation.UserId;
import kr.co.zeppy.global.error.ApplicationError;
import kr.co.zeppy.global.error.ApplicationException;
import kr.co.zeppy.location.dto.CurrentLocationModeResponse;
import kr.co.zeppy.location.dto.FriendInfo;
import kr.co.zeppy.location.dto.LocationModeTimerResponse;
import kr.co.zeppy.location.entity.LocationMode;
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

//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new ApplicationException(ApplicationError.USER_ID_NOT_FOUND));
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
}
