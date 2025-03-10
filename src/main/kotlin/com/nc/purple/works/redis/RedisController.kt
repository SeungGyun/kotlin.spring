package com.nc.purple.works.redis

import com.nc.purple.works.redis.service.RedisService
import kotlinx.coroutines.runBlocking
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/redis")
class RedisController(private val redisService: RedisService) {

    @PostMapping("/set")
    fun setValue(@RequestParam key: String, @RequestParam value: String) = runBlocking {
        redisService.setValue(key, value)
        "Success: $key -> $value"
    }

    @GetMapping("/get")
    fun getValue(@RequestParam key: String): String? = runBlocking {
        redisService.getValue(key)
    }
}
