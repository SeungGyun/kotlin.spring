package com.nc.purple.works.mysql.repository

import com.nc.purple.works.mysql.Game
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface GameRepository : ReactiveCrudRepository<Game, Long> {
    fun findByGameCode(gameCode: String): Mono<Game>
    fun findAllByGroupKey(groupKey: String): Flux<Game>
}