package ar.unq.ttip.neohub.repository

import ar.unq.ttip.neohub.model.Room
import org.springframework.data.jpa.repository.JpaRepository

interface RoomRepository : JpaRepository<Room, Long> {

}