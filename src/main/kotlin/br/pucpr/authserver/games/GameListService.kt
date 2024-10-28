package br.pucpr.authserver.games

import br.pucpr.authserver.errors.ForbiddenException
import br.pucpr.authserver.errors.NotFoundException
import br.pucpr.authserver.users.User
import br.pucpr.authserver.users.UserRepository
import org.springframework.stereotype.Service
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Service
class GameListService(
    private val gameListRepository: GameListRepository,
    private val userRepository: UserRepository
) {

    private val logger: Logger = LoggerFactory.getLogger(GameListService::class.java)

    fun createGameList(user: User): GameList {
        logger.info("Creating game list for user: ${user.id}")
        if (gameListRepository.findByUserId(user.id!!) != null) {
            throw ForbiddenException("User already has a game list")
        }
        return gameListRepository.save(GameList(user = user))
    }

    fun findUserGameList(user: User, sortDir: SortDir?): GameList {
        logger.info("Finding game list for user: ${user.id}")
        val gameList = gameListRepository.findByUserId(user.id!!)
            ?: throw NotFoundException("Game list not found")

        return sortGameList(gameList, sortDir)
    }

    fun findAllLists(sortDir: SortDir?): List<GameList> {
        logger.info("Finding all game lists")
        val gameLists = gameListRepository.findAll()
        return gameLists.map { sortGameList(it, sortDir) }
    }

    private fun sortGameList(gameList: GameList, sortDir: SortDir?): GameList {
        return when (sortDir) {
            SortDir.lASC -> gameList.apply { games.sortBy { it.name } }
            SortDir.lDESC -> gameList.apply { games.sortByDescending { it.name } }
            SortDir.hASC -> gameList.apply { games.sortBy { it.hours } }
            SortDir.hDESC -> gameList.apply { games.sortByDescending { it.hours } }
            else -> gameList
        }
    }

    fun addGameToList(gameList: GameList, name: String, hours: Int): Game {
        logger.info("Adding game '$name' to listId: ${gameList.id}")
        val duplicate = gameList.games.any { it.name.equals(name, ignoreCase = true) }
        if (duplicate) {
            throw IllegalArgumentException("A game with the same name already exists in the list")
        }

        val game = Game(name = name, hours = hours, gameList = gameList)
        gameList.games.add(game)

        gameListRepository.save(gameList)

        return game
    }

    fun deleteGameList(id: Long) {
        gameListRepository.deleteById(id)
    }

    fun updateGame(gameList: GameList, gameId: Long, name: String?, hours: Int?): Game {
        logger.info("Updating gameId: $gameId in listId: ${gameList.id}")
        val game = gameList.games.find { it.id == gameId }
            ?: throw NotFoundException("Game not found in user's list")

        name?.let {
            val duplicate = gameList.games.any { existingGame ->
                existingGame.name.equals(it, ignoreCase = true) && existingGame.id != gameId
            }
            if (duplicate) {
                throw IllegalArgumentException("A game with the same name already exists in the list")
            }
            game.name = it
        }

        hours?.let { game.hours = it }

        gameListRepository.save(gameList)

        return game
    }

    fun removeGameFromList(gameList: GameList, gameId: Long?, gameName: String?) {
        logger.info("Removing game from listId: ${gameList.id}, gameId: $gameId, gameName: '$gameName'")
        val game = gameList.games.find {
            (gameId != null && it.id == gameId) ||
                    (gameName != null && it.name.equals(gameName, ignoreCase = true))
        } ?: throw NotFoundException("Game not found in the list")

        gameList.games.remove(game)
        gameListRepository.save(gameList)
    }
}
