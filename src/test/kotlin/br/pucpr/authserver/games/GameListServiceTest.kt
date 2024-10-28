package br.pucpr.authserver.games

import br.pucpr.authserver.errors.NotFoundException
import br.pucpr.authserver.users.UserRepository
import br.pucpr.authserver.users.user
import io.kotest.matchers.shouldBe
import io.mockk.checkUnnecessaryStub
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GameListServiceTest {
    private val gameListRepositoryMock = mockk<GameListRepository>(relaxed = true)
    private val userRepositoryMock = mockk<UserRepository>(relaxed = true)
    private val service = GameListService(
        gameListRepository = gameListRepositoryMock,
        userRepository = userRepositoryMock
    )

    @AfterEach
    fun cleanUp() {
        checkUnnecessaryStub()
    }

    @Test
    fun `addGameToList adds a new game to the list`() {
        val user = user(id = 1)
        val gameList = GameList(user = user)
        val gameName = "Game A"
        val hours = 10

        every { gameListRepositoryMock.save(any()) } returns gameList

        val result = service.addGameToList(gameList, gameName, hours)

        result.name shouldBe gameName
        result.hours shouldBe hours
        gameList.games.size shouldBe 1
    }

    @Test
    fun `removeGameFromList throws NotFoundException if game does not exist`() {
        val user = user(id = 1)
        val gameList = GameList(user = user)

        assertThrows<NotFoundException> {
            service.removeGameFromList(gameList, gameId = 1, gameName = null)
        }
    }
}
