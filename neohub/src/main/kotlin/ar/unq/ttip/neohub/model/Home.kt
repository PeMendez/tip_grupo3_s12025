package ar.unq.ttip.neohub.model

import com.fasterxml.jackson.annotation.JsonManagedReference
import jakarta.persistence.*

@Entity
data class Home(
    @Id @GeneratedValue val id: Long = 0,

    @Column(unique = true, nullable = false)
    val name: String,

    @Column(nullable = false)
    val accessKey: String, // Clave para unirse a la home

    @OneToMany(mappedBy = "home", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JsonManagedReference
    val userHomes: MutableList<UserHome> = mutableListOf(),

    @OneToMany(mappedBy = "home", cascade = [CascadeType.PERSIST, CascadeType.MERGE], fetch = FetchType.LAZY)
    @JsonManagedReference
    val rooms: MutableList<Room> = mutableListOf()
) {
    fun addRoom(room: Room) {
        rooms.add(room)
        room.home = this
    }

    fun addUserHome(userHome: UserHome) {
        userHomes.add(userHome)
        userHome.home = this
    }

    fun getAdmins(): List<User> {
        return userHomes.filter { it.role == Role.ADMIN }.mapNotNull { it.user }
    }

    fun getUsers(): List<User> {
        return userHomes.mapNotNull { it.user }
    }
    override fun toString(): String {
        return "Home($name, $accessKey)"
    }
}
