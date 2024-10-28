package br.pucpr.authserver.games

import org.springframework.data.jpa.repository.JpaRepository

interface GameListRepository : JpaRepository<GameList, Long> {
    fun findByUserId(userId: Long): GameList?
}
