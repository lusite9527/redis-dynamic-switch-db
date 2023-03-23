package com.yebuxiu.controller;


import com.yebuxiu.helper.RedisHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("/switch-redis-db")
public class TestEnhanceDataRedisController {

    /**
     * 默认数据源对应的redisHelper
     */
    @Autowired
    @Qualifier("redisHelper")
    private RedisHelper redisHelper;


    @GetMapping("/test")
    public ResponseEntity<String> testChangeDb2(@RequestParam int db, @RequestParam String key, @RequestParam String value) {
        redisHelper.strSetWithDb(db, key, value, 1000, null);
        return new ResponseEntity<>(redisHelper.strGetWithDb(db, key), HttpStatus.OK);
    }
}
