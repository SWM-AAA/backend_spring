package kr.co.zeppy.location.service;

import jakarta.transaction.Transactional;
import kr.co.zeppy.location.dto.LocationModeTimerResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class LocationModeService {

    public LocationModeTimerResponse getTimes() {

        List<Integer> timeList = Arrays.asList(2, 4, 8, 24);
        return LocationModeTimerResponse.builder()
                .times(timeList)
                .build();
    }
}
