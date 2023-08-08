package kr.co.zeppy.global.redis.service;

import org.springframework.data.redis.core.RedisTemplate;

import kr.co.zeppy.global.error.ApplicationError;
import kr.co.zeppy.global.error.ApplicationException;
import kr.co.zeppy.global.redis.dto.LocationAndBatteryRequest;
import org.springframework.stereotype.Service;

@Service
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";
    private static final String BATTERY = "battery";
    private static final String IS_CHARGING = "isCharging";
    private static final String USER_PREFIX = "user_";
    
    public RedisService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    public void updateLocationAndBattery(String userId, LocationAndBatteryRequest locationAndBatteryRequest) {
        String key = USER_PREFIX + userId;
        try {
            redisTemplate.opsForHash().put(key, LATITUDE, locationAndBatteryRequest.getLatitude());
            redisTemplate.opsForHash().put(key, LONGITUDE, locationAndBatteryRequest.getLongitude());
            redisTemplate.opsForHash().put(key, BATTERY, locationAndBatteryRequest.getBattery());
            redisTemplate.opsForHash().put(key, IS_CHARGING, locationAndBatteryRequest.getIsCharging().toString());
        } catch (Exception e) {
            throw new ApplicationException(ApplicationError.REDIS_SERVER_UNAVAILABLE);
        }
    }
}
