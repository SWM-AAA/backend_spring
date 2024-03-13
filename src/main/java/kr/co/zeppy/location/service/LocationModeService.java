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
        return LocationModeTimerResponse.builder()
                .shortest(2)
                .second_shortest(4)
                .medium(8)
                .longest(24)
                .build();
    }
}
