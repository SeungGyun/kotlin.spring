package com.nc.purple.works.redis.config

import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisClusterConfiguration
import org.springframework.data.redis.connection.RedisPassword
import org.springframework.data.redis.connection.RedisSentinelConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration
import org.springframework.data.redis.core.ReactiveStringRedisTemplate

@Configuration
class RedisConfig(
    @Value("\${spring.redis.mode}") private val mode: String,
    @Value("\${spring.redis.password}") private val redisPassword: String?,
    @Value("\${spring.redis.sentinel.master}") private val sentinelMaster: String,
    @Value("\${spring.redis.sentinel.nodes}") private val sentinelNodes: String,
    @Value("\${spring.redis.cluster.nodes}") private val clusterNodes: String,
    @Value("\${spring.redis.lettuce.pool.max-active}") private val maxActive: Int,
    @Value("\${spring.redis.lettuce.pool.max-idle}") private val maxIdle: Int,
    @Value("\${spring.redis.lettuce.pool.min-idle}") private val minIdle: Int
) {

    @Bean
    fun redisConnectionFactory(): LettuceConnectionFactory {
        val poolConfig = GenericObjectPoolConfig<Any>().apply {
            maxTotal = maxActive
            this.maxIdle = maxIdle
            this.minIdle = minIdle
        }

        val clientConfig = LettucePoolingClientConfiguration.builder()
            .poolConfig(poolConfig)
            .build()

        return when (mode.lowercase()) {
            "sentinel" -> {
                val sentinelConfig = RedisSentinelConfiguration(sentinelMaster, sentinelNodes.split(",").toSet())
                sentinelConfig.password = RedisPassword.of(redisPassword)
                LettuceConnectionFactory(sentinelConfig, clientConfig)
            }

            "cluster" -> {
                val clusterConfig = RedisClusterConfiguration(clusterNodes.split(","))
                LettuceConnectionFactory(clusterConfig, clientConfig)
            }

            else -> throw IllegalArgumentException("Unsupported Redis mode: $mode")
        }
    }

    @Bean
    fun reactiveRedisTemplate(factory: LettuceConnectionFactory): ReactiveStringRedisTemplate {
        return ReactiveStringRedisTemplate(factory)
    }
}
