package ar.unq.ttip.neohub.service

import ar.unq.ttip.neohub.dto.*
import ar.unq.ttip.neohub.model.Home
import ar.unq.ttip.neohub.model.Room
import ar.unq.ttip.neohub.model.User
import ar.unq.ttip.neohub.model.devices.DeviceType
import ar.unq.ttip.neohub.repository.HomeRepository
import ar.unq.ttip.neohub.repository.RoomRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class HomeService(
    private val homeRepository: HomeRepository,
    private val roomRepository: RoomRepository,
    private val roomService: RoomService,
    private val userService: UserService,
    private val userHomeService: UserHomeService,
    private val deviceService: DeviceService,
    private val ruleService: RuleService
){
    fun getHomeForUser(userId: Long): Home {
        val homes = homeRepository.findByUserId(userId)
        if (homes.isEmpty()) {
            throw IllegalArgumentException("El usuario no está asociado a ninguna home.")
        }
        return homes.first() // Retornar la primera en caso de que haya múltiples
    }

    fun getHomeForUserNulleable(userId: Long): Home? {
        val homes = homeRepository.findByUserId(userId)
        return homes.firstOrNull()
    }

    fun getAdminHomeForUser(userId: Long): Home {
        val adminHomes = homeRepository.findAdminHomeByUserId(userId)
        if (adminHomes.isEmpty()) {
            throw IllegalArgumentException("El usuario no es admin de ninguna home.")
        }
        return adminHomes.first() // Retornar la primera en caso de que haya múltiples
    }

    fun getRooms(user: User): List<Room> {
        val home = getHomeForUser(user.id)
        return home.rooms
    }

    fun getDevicesByTypesInHome(types: List<DeviceType>, userId: Long): List<DeviceDTO> {
        val home = getHomeForUser(userId)
        return homeRepository.findDevicesByHomeIdAndType(home.id, types).map { it.toDTO() }
    }

    /*fun getRulesInHome(userId: Long): List<RuleDTO>{
        val user = userService.getUserById(userId)
        val home = getHomeForUser(userId)
        val homeId = home.id
        if (!home.getAdmins().contains(user)) {
            TODO("chequear reglas solo que pueda ver ")
        } else {
            return homeRepository.findRulesByHomeId(homeId).map { it.toDTO() }
        }
    }*/

    fun getRulesInHome(userId: Long): List<RuleDTO> {
        val user = userService.getUserById(userId)
        val home = getHomeForUser(userId)
        val isAdmin = home.getAdmins().contains(user)

        val rulesWithConditions = homeRepository.findRulesWithConditions(user.username, isAdmin)
        val completeRules = homeRepository.fetchActionsForRules(rulesWithConditions, user.username, isAdmin)

        return completeRules.map { it.toDTO() }
    }



    @Transactional
    fun addRoomToHome(user: User, roomName: String): Room {
        val home = getHomeForUser(user.id)
        val room = Room(name = roomName, home = home)
        return roomRepository.save(room)
    }

    @Transactional
    fun removeRoomFromHome(user: User, roomId: Long) {
        val home = getHomeForUser(user.id)
        val room = roomRepository.findById(roomId)
            .orElseThrow { IllegalArgumentException("Habitación no encontrada") }
        if (room.home?.id != home.id) {
            throw IllegalAccessException("La habitación no pertenece a este hogar")
        }
        //Desconfigurar todos los dispositivos de este room
        val devices = room.deviceList.toList()
        devices.forEach { device ->
            roomService.removeDeviceFromRoom(device.id,roomId)
        }
        roomRepository.delete(room)
    }

    @Transactional
    fun saveHome(home: Home): Home {
        return homeRepository.save(home)
    }

    fun getAllMembers(homeId: Long) : List<UserHomeDTO> {
        return homeRepository.findAllMembersByHome(homeId).map { it.toDTO() }
    }

    fun deleteMember(homeId: Long, userId: Long)  {
        val user = userService.getUserById(userId)
        deviceService.unregisterAllDevicesForUser(user)
        userHomeService.deleteMember(homeId, user, user.username)
    }
}
