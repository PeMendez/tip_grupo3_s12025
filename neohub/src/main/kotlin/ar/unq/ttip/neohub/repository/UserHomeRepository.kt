package ar.unq.ttip.neohub.repository

import ar.unq.ttip.neohub.model.User
import ar.unq.ttip.neohub.model.UserHome
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface UserHomeRepository: JpaRepository<UserHome, Long> {
    fun findByUser(user: User): List<UserHome>

    @Query("SELECT uh FROM UserHome uh JOIN uh.user u WHERE u.username = :username")
    fun findByUsername(username: String): List<UserHome>

    @Query("SELECT uh FROM UserHome uh JOIN uh.user u JOIN uh.home h WHERE u.username = :username AND h.id = :homeId")
    fun findByUsernameAndHomeId(username: String, homeId: Long): UserHome?

}