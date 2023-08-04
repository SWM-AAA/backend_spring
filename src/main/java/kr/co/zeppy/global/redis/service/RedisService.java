package kr.co.zeppy.global.redis.service;

import org.springframework.data.redis.core.RedisTemplate;
import kr.co.zeppy.global.redis.dto.LocationAndBatteryRequest;
import org.springframework.stereotype.Service;

@Service
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;
    
    public RedisService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    public boolean updateLocationAndBattery(LocationAndBatteryRequest locationAndBatteryRequest) {
        try {
            redisTemplate.opsForValue().set("latitude", locationAndBatteryRequest.getLatitude());
            redisTemplate.opsForValue().set("longitude", locationAndBatteryRequest.getLongitude());
            redisTemplate.opsForValue().set("battery", locationAndBatteryRequest.getBattery());
            redisTemplate.opsForValue().set("isCharging", locationAndBatteryRequest.getIsCharging().toString());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
