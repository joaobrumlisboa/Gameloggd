package br.pucpr.authserver.games

import br.pucpr.authserver.errors.ForbiddenException
import br.pucpr.authserver.security.UserToken
import br.pucpr.authserver.errors.NotFoundException
import br.pucpr.authserver.users.UserRepository
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/game-lists")
class GameListController(
    private val service: GameListService,
    private val userRepository: UserRepository
) {
    @SecurityRequirement(name = "AuthServer")
    @GetMapping
    fun getGameList(
        auth: Authentication,
        @RequestParam(required = false) sortDir: SortDir?
    ): List<GameList> {
        val token = auth.principal as UserToken
        val user = userRepository.findById(token.id)
            .orElseThrow { NotFoundException("User not found") }

        val gameList = if (token.isAdmin) {
            service.findAllLists(sortDir)
        } else {
            listOf(service.findUserGameList(user, sortDir))
        }

        return gameList
    }

    @SecurityRequirement(name = "AuthServer")
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    fun createGameList(auth: Authentication): GameList {
        val token = auth.principal as UserToken
        val user = userRepository.findById(token.id)
            .orElseThrow { NotFoundException("User not found") }

        return service.createGameList(user)
    }

    @SecurityRequirement(name = "AuthServer")
    @PostMapping("/games")
    @PreAuthorize("hasRole('USER')")
    fun addGameToList(
        @RequestParam name: String,
        @RequestParam hours: Int,
        auth: Authentication
    ): Game {
        val token = auth.principal as UserToken
        val user = userRepository.findById(token.id)
            .orElseThrow { NotFoundException("User not found") }

        val list = service.findUserGameList(user, null)
        return service.addGameToList(list, name, hours)
    }

    @SecurityRequirement(name = "AuthServer")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteGameList(@PathVariable id: Long) {
        service.deleteGameList(id)
    }

    @SecurityRequirement(name = "AuthServer")
    @PatchMapping("/games/{gameId}")
    @PreAuthorize("hasRole('USER')")
    fun updateGame(
        @PathVariable gameId: Long,
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) hours: Int?,
        auth: Authentication
    ): Game {
        val token = auth.principal as UserToken
        val user = userRepository.findById(token.id)
            .orElseThrow { NotFoundException("User not found") }

        val gameList = service.findUserGameList(user, null)

        return service.updateGame(gameList, gameId, name, hours)
    }

    @SecurityRequirement(name = "AuthServer")
    @DeleteMapping("/games")
    @PreAuthorize("hasRole('USER')")
    fun removeGameFromList(
        @RequestParam(required = false) gameId: Long?,
        @RequestParam(required = false) gameName: String?,
        auth: Authentication
    ) {
        val token = auth.principal as UserToken
        val user = userRepository.findById(token.id)
            .orElseThrow { NotFoundException("User not found") }

        if (token.isAdmin) throw ForbiddenException("Admin cannot remove games")

        if (gameId == null && gameName.isNullOrBlank()) {
            throw IllegalArgumentException("Either gameId or gameName must be provided")
        }

        val gameList = service.findUserGameList(user, null)
        service.removeGameFromList(gameList, gameId, gameName)
    }
}
