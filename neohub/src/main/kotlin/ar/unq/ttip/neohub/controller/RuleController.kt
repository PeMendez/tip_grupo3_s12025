package ar.unq.ttip.neohub.controller

import ar.unq.ttip.neohub.dto.CreateRuleRequest
import ar.unq.ttip.neohub.dto.DeviceDTO
import ar.unq.ttip.neohub.dto.RuleDTO
import ar.unq.ttip.neohub.model.ActionType
import ar.unq.ttip.neohub.model.Attribute
import ar.unq.ttip.neohub.model.Operator
import ar.unq.ttip.neohub.model.devices.DeviceType
import ar.unq.ttip.neohub.repository.RuleRepository
import ar.unq.ttip.neohub.service.HomeService
import ar.unq.ttip.neohub.service.RuleService
import ar.unq.ttip.neohub.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/rule")
class RuleController(
    private val ruleService: RuleService,
    private val ruleRepository: RuleRepository,
    private val userService: UserService,
    private val homeService: HomeService
) {

    @GetMapping("/{deviceId}")
    fun getRulesForDevice(@PathVariable deviceId: Long): List<RuleDTO> {
        return ruleService.getRulesForDevice(deviceId)
    }

    @PostMapping
    fun createRule(@RequestBody request: CreateRuleRequest): RuleDTO {
        return ruleService.createRule(request)
    }

    @DeleteMapping("/{ruleId}")
    fun deleteRule(@PathVariable ruleId: Long) {
        ruleRepository.deleteById(ruleId)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleIllegalArgument(ex: IllegalArgumentException): String {
        return ex.message ?: "Solicitud inválida"
    }

    @GetMapping("/{deviceType}/attributes")
    fun getSupportedAtrributes(@PathVariable deviceType: String) : List <Attribute>{
        val type = DeviceType.fromString(deviceType)
        return DeviceType.getSupportedAttributes(type)
    }

    @GetMapping("/{attributeType}/operators")
    fun getSupportedOperators(@PathVariable attributeType: String) : List <Operator>{
        val attribute = Attribute.fromString(attributeType)
        return Attribute.getSupportedOperators(attribute)
    }

    @GetMapping("/{deviceType}/actions")
    fun getSupportedActions(@PathVariable deviceType: String) : List<ActionType>{
        val type = DeviceType.fromString(deviceType)
        return DeviceType.getSupportedActions(type)
    }

    @GetMapping("/{actionType}/devices")
    fun getSupportedDevices(@PathVariable actionType: String) : List<DeviceType>{
        val type = ActionType.fromString(actionType)
        return ActionType.getSupportedDevicesFor(type)
    }

    @GetMapping("/device-options")
    fun getDevicesByTypesInHome(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestParam types: List<String>
    ): List<DeviceDTO> {
        val user = userService.getUserByUsername(userDetails.username)
        val deviceTypes = types.map { DeviceType.fromString(it) }
        return homeService.getDevicesByTypesInHome(deviceTypes, user.id)
    }

    @GetMapping("/allRules")
    fun getAllRules(
        @AuthenticationPrincipal userDetails: UserDetails): List<RuleDTO> {
        val user = userService.getUserByUsername(userDetails.username)
        return homeService.getRulesInHome(user.id)
    }
}