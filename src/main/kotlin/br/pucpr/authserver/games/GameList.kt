package br.pucpr.authserver.games

import br.pucpr.authserver.users.User
import jakarta.persistence.*

@Entity
@Table(name = "GameLists")
class GameList(
    @Id @GeneratedValue
    var id: Long? = null,

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @OneToMany(mappedBy = "gameList", cascade = [CascadeType.ALL], orphanRemoval = true)
    val games: MutableList<Game> = mutableListOf()
)
