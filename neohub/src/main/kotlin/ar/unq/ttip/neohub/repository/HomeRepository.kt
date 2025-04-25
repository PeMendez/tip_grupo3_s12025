package ar.unq.ttip.neohub.repository

import ar.unq.ttip.neohub.model.Home
import org.springframework.data.jpa.repository.JpaRepository

interface HomeRepository : JpaRepository<Home, Long> {

}