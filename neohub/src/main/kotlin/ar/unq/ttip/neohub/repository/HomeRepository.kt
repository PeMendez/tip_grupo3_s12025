package ar.unq.ttip.neohub.repository

import ar.unq.ttip.neohub.model.Home
import ar.unq.ttip.neohub.model.User
import org.springframework.data.jpa.repository.JpaRepository

interface HomeRepository : JpaRepository<Home, Long> {
    fun findByUser(user: User): Home?
}