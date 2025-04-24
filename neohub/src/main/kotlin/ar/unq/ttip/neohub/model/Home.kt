package ar.unq.ttip.neohub.model

import jakarta.persistence.*

@Entity
data class Home(
    @Id @GeneratedValue val id: Long = 0,

    @OneToOne
    @JoinColumn(name = "user_id")
    val user: User,

    @OneToMany(mappedBy = "home", cascade = [CascadeType.ALL], orphanRemoval = true)
    val rooms: List<Room> = mutableListOf()
)