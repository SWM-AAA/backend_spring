package kr.co.zeppy.location.service;

import kr.co.zeppy.location.dto.LocationModeTimerResponse;
import kr.co.zeppy.user.entity.UserChatRoom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class LocationModeServiceTest {

    @InjectMocks
    private LocationModeService locationModeService;

    @Test
    void getTimes() {
        // Given

        // When
        LocationModeTimerResponse response = locationModeService.getTimes();

        // Then
        assertAll(
                () -> assertEquals(response.getShortest(), 2),
                () -> assertEquals(response.getSecondShortest(), 4),
                () -> assertEquals(response.getMedium(), 8),
                () -> assertEquals(response.getLongest(), 24)
        );
    }
}
