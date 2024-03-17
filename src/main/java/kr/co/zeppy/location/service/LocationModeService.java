package kr.co.zeppy.location.service;

import jakarta.transaction.Transactional;
import kr.co.zeppy.global.annotation.UserId;
import kr.co.zeppy.location.dto.CurrentLocationModeResponse;
import kr.co.zeppy.location.dto.FriendInfo;
import kr.co.zeppy.location.dto.LocationModeTimerResponse;
import kr.co.zeppy.location.repository.LocationModeRepository;
import kr.co.zeppy.user.repository.FriendshipRepository;
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

    public LocationModeTimerResponse getTimes() {

        List<Integer> timeList = Arrays.asList(2, 4, 8, 24);
        return LocationModeTimerResponse.builder()
                .times(timeList)
                .build();
    }

    public CurrentLocationModeResponse getLocationMode(Long userId) {

        List<FriendInfo> accurateList = locationModeRepository.findAccurateFriendsByUserId(userId);
        List<FriendInfo> ambiguousList = locationModeRepository.findAmbiguousFriendsByUserId(userId);
        List<FriendInfo> pinnedList = locationModeRepository.findPinnedFriendsByUserId(userId);

        return CurrentLocationModeResponse.builder()
                .accurate(accurateList)
                .ambiguous(ambiguousList)
                .pinned(pinnedList)
                .build();
    }
}
