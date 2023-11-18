package kr.co.zeppy.global.redis.service;

import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;
import kr.co.zeppy.global.error.ApplicationError;
import kr.co.zeppy.global.error.ApplicationException;
import kr.co.zeppy.global.redis.dto.FriendLocationAndBattery;
import kr.co.zeppy.global.redis.dto.FriendLocationAndBatteryResponse;
import kr.co.zeppy.global.redis.dto.LocationAndBatteryRequest;
import kr.co.zeppy.user.entity.Friendship;
import kr.co.zeppy.user.entity.User;
import kr.co.zeppy.user.repository.FriendshipRepository;
import kr.co.zeppy.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j; 

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String USER_PREFIX = "user_";
    private static final String RESULT = "result";

    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final ObjectMapper objectMapper;
    

    public void updateLocationAndBattery(String userId, LocationAndBatteryRequest locationAndBatteryRequest) {
        String key = USER_PREFIX + userId;
        try {
            String jsonValue = objectMapper.writeValueAsString(locationAndBatteryRequest);
            redisTemplate.opsForValue().set(key, jsonValue);

        } catch (Exception e) {
            throw new ApplicationException(ApplicationError.REDIS_SERVER_UNAVAILABLE);
        }
    }

    
    public Map<String, Map<String, LocationAndBatteryRequest>> getAllUsersLocationAndBattery() {
        List<User> allUsers = userRepository.findAll();

        Map<String, LocationAndBatteryRequest> allUsersData = new HashMap<>();
        Map<String, Map<String, LocationAndBatteryRequest>> wrappedResponse = new HashMap<>();
        try {
            for (User user : allUsers) {
                String key = USER_PREFIX + user.getId();
                String jsonValue = redisTemplate.opsForValue().get(key);
                if (jsonValue == null) {
                    continue;
                }
                LocationAndBatteryRequest data = objectMapper.readValue(jsonValue, LocationAndBatteryRequest.class);
                allUsersData.put(key, data);
            }
        } catch (Exception e) {
            log.error("Redis error: ", e);
            throw new ApplicationException(ApplicationError.REDIS_SERVER_UNAVAILABLE);
        }
        wrappedResponse.put(RESULT, allUsersData);

        return wrappedResponse;
    }

    
    // 친구 위치, 배터리 반환
    public FriendLocationAndBatteryResponse getFriendLocationAndBattery(Long userId) {
        List<User> friendList = friendshipRepository.findAcceptedFriendsByUserId(userId);
        List<FriendLocationAndBattery> friendLocationAndBatteryList = new ArrayList<>();

        for (User friend : friendList) {
            String key = USER_PREFIX + friend.getId();
            String jsonValue = redisTemplate.opsForValue().get(key);
            
            if (jsonValue != null) {
                try {
                    FriendLocationAndBattery data = objectMapper.readValue(jsonValue, FriendLocationAndBattery.class);
                    friendLocationAndBatteryList.add(data);
                } catch (Exception e) {
                    log.error("Redis error: ", e);
                    throw new ApplicationException(ApplicationError.REDIS_SERVER_UNAVAILABLE);
                }
            }
        }

        return FriendLocationAndBatteryResponse.builder()
                .message("success")
                .data(friendLocationAndBatteryList)
                .build();
    }
}
