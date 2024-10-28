package br.pucpr.authserver.games

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

@Entity
@Table(name = "Games")
class Game(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @NotBlank
    var name: String,

    @NotNull
    var hours: Int,

    @ManyToOne
    @JoinColumn(name = "list_id", nullable = false)
    @JsonIgnore
    var gameList: GameList
)

