package ar.unq.ttip.neohub.model.devices

import ar.unq.ttip.neohub.model.ActionType
import ar.unq.ttip.neohub.model.Attribute

enum class DeviceType {
    SMART_OUTLET,
    TEMPERATURE_SENSOR,
    OPENING_SENSOR,
    DIMMER;

    companion object {
        fun fromString(value: String): DeviceType {
            return entries.find { it.name.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown DeviceType: $value")
        }

        private val supportedAttributes: Map<DeviceType, List<Attribute>> = mapOf(
            TEMPERATURE_SENSOR to listOf(Attribute.TEMPERATURE),
            SMART_OUTLET to listOf(Attribute.IS_ON),
            OPENING_SENSOR to listOf(Attribute.IS_OPEN),
            DIMMER to listOf(Attribute.BRIGHTNESS)
        )

        fun getSupportedAttributes(type: DeviceType): List<Attribute> {
            return supportedAttributes[type] ?: emptyList()
        }

        private val supportedActions: Map<DeviceType, List<ActionType>> = mapOf(
            SMART_OUTLET to listOf(ActionType.TURN_ON, ActionType.TURN_OFF),
            DIMMER to listOf(ActionType.SET_BRIGHTNESS),
        )

        fun getSupportedActions(type: DeviceType): List<ActionType> {
            return supportedActions[type] ?: emptyList()
        }

        private val actionableDevices: Map<DeviceType, List<DeviceType>> = mapOf(
            SMART_OUTLET to listOf(SMART_OUTLET),
            TEMPERATURE_SENSOR to listOf(SMART_OUTLET, DIMMER),
            OPENING_SENSOR to listOf(SMART_OUTLET, DIMMER),
        )

        fun getActionableDevices(type: DeviceType): List<DeviceType> {
            return actionableDevices[type] ?: emptyList()
        }
    }

    override fun toString(): String {
        return name.lowercase()
    }
}