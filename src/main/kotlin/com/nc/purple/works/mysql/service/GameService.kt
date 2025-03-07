package com.nc.purple.works.mysql.service

import com.nc.purple.works.mysql.Game
import com.nc.purple.works.mysql.repository.GameRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class GameService(private val gameRepository: GameRepository) {

    fun getGameByCode(gameCode: String): Mono<Game> {
        return gameRepository.findByGameCode(gameCode)
    }

    fun getGamesByGroup(groupKey: String): Flux<Game> {
        return gameRepository.findAllByGroupKey(groupKey)
    }

    fun createGame(game: Game): Mono<Game> {
        return gameRepository.save(game)
    }
}