package com.nc.purple.works.mysql

import com.nc.purple.works.mysql.service.GameService
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/games")
class GameController(private val gameService: GameService) {

    @GetMapping("/{gameCode}")
    fun getGame(@PathVariable gameCode: String): Mono<Game> {
        return gameService.getGameByCode(gameCode)
    }

    @GetMapping("/group/{groupKey}")
    fun getGamesByGroup(@PathVariable groupKey: String): Flux<Game> {
        return gameService.getGamesByGroup(groupKey)
    }

    @PostMapping
    fun createGame(@RequestBody game: Game): Mono<Game> {
        return gameService.createGame(game)
    }
}