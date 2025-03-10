package com.nc.purple.works.redis.service

import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Service

@Service
class RedisService(private val reactiveRedisTemplate: ReactiveStringRedisTemplate) {

    suspend fun setValue(key: String, value: String) {
        reactiveRedisTemplate.opsForValue().set(key, value).awaitFirstOrNull()
    }

    suspend fun getValue(key: String): String? {
        return reactiveRedisTemplate.opsForValue().get(key).awaitFirstOrNull()
    }
}
