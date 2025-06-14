package ar.unq.ttip.neohub.model

import com.fasterxml.jackson.annotation.JsonManagedReference
import jakarta.persistence.*

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true, nullable = false)
    val username: String,

    @Column(nullable = false)
    val password: String,

    val enabled: Boolean = true,

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.EAGER, orphanRemoval = true)
    @JsonManagedReference
    val userHomes: MutableList<UserHome> = mutableListOf()
) {
    fun addHome(userHome: UserHome) {
        userHomes.add(userHome)
        userHome.user = this
    }


}

