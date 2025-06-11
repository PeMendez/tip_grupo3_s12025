package ar.unq.ttip.neohub.repository

import ar.unq.ttip.neohub.model.UserHome
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserHomeRepository: JpaRepository<UserHome, Long> {}