package ar.unq.ttip.neohub.model

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

//    @OneToOne(mappedBy = "user", cascade = [CascadeType.ALL])
//    val home: Home? = null

) {
    override fun toString(): String {
        return "User(id=$id, username='$username', enabled=$enabled)" 
    }
}
