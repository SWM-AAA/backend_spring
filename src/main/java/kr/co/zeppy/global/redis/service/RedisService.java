package kr.co.zeppy.global.redis.service;

import org.springframework.data.redis.core.RedisTemplate;
import kr.co.zeppy.global.redis.dto.LocationAndBatteryRequest;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;
    
    public RedisService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    public void updateLocationAndBattery(LocationAndBatteryRequest locationAndBatteryRequest) throws Exception {
        redisTemplate.opsForValue().set("latitude", locationAndBatteryRequest.getLatitude());
        redisTemplate.opsForValue().set("longitude", locationAndBatteryRequest.getLongitude());
        redisTemplate.opsForValue().set("battery", locationAndBatteryRequest.getBattery());
        redisTemplate.opsForValue().set("isCharging", locationAndBatteryRequest.getIsCharging().toString());
    }
}
