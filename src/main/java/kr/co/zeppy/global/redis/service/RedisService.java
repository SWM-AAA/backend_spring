package kr.co.zeppy.global.redis.service;

import org.springframework.data.redis.core.RedisTemplate;
import kr.co.zeppy.global.redis.dto.LocationAndBatteryRequest;
import org.springframework.stereotype.Service;

@Service
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";
    private static final String BATTERY = "battery";
    private static final String IS_CHARGING = "isCharging";
    
    public RedisService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    public boolean updateLocationAndBattery(String userId, LocationAndBatteryRequest locationAndBatteryRequest) {
        try {
            String key = "user_" + userId;
            redisTemplate.opsForHash().put(key, LATITUDE, locationAndBatteryRequest.getLatitude());
            redisTemplate.opsForHash().put(key, LONGITUDE, locationAndBatteryRequest.getLongitude());
            redisTemplate.opsForHash().put(key, BATTERY, locationAndBatteryRequest.getBattery());
            redisTemplate.opsForHash().put(key, IS_CHARGING, locationAndBatteryRequest.getIsCharging().toString());

            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
