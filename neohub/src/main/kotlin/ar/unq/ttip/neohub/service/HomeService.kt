package ar.unq.ttip.neohub.service

import ar.unq.ttip.neohub.model.Home
import ar.unq.ttip.neohub.model.Room
import ar.unq.ttip.neohub.model.User
import ar.unq.ttip.neohub.repository.HomeRepository
import ar.unq.ttip.neohub.repository.RoomRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class HomeService(
    private val homeRepository: HomeRepository,
    private val roomRepository: RoomRepository
){
    fun getHomeByUser(user: User): Home? {
        return homeRepository.findByUser(user)
    }

    fun getRooms(user: User): List<Room> {
        val home = homeRepository.findByUser(user)
            ?: throw IllegalArgumentException("El usuario no tiene un hogar asignado")
        return home.rooms
    }

    @Transactional
    fun addRoomToHome(user: User, roomName: String): Room {
        val home = homeRepository.findByUser(user)
            ?: throw IllegalArgumentException("El usuario no tiene un hogar asignado")
        val room = Room(name = roomName, home = home)
        return roomRepository.save(room)
    }

    @Transactional
    fun removeRoomFromHome(user: User, roomId: Long) {
        val home = homeRepository.findByUser(user)
            ?: throw IllegalArgumentException("El usuario no tiene un hogar asignado")

        val room = roomRepository.findById(roomId)
            .orElseThrow { IllegalArgumentException("Habitación no encontrada") }

        if (room.home?.id != home.id) {
            throw IllegalAccessException("La habitación no pertenece a este hogar")
        }

        roomRepository.delete(room)
    }
}
