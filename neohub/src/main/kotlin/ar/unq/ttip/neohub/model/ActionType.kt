package ar.unq.ttip.neohub.model

import ar.unq.ttip.neohub.model.devices.DeviceType

enum class ActionType (val supportedDeviceTypes: List<DeviceType>) {
    TURN_ON(listOf(DeviceType.SMART_OUTLET)),
    TURN_OFF(listOf(DeviceType.SMART_OUTLET)),
    NOTIFY(listOf(DeviceType.TEMPERATURE_SENSOR, DeviceType.OPENING_SENSOR)),
    SET_BRIGHTNESS(listOf(DeviceType.DIMMER));

    fun supports(deviceType: DeviceType): Boolean {
        return supportedDeviceTypes.contains(deviceType)
    }

    companion object {
        fun getSupportedDevicesFor(actionType: ActionType): List<DeviceType> {
            return actionType.supportedDeviceTypes
        }

        fun fromString(value: String): ActionType {
            return ActionType.entries.find { it.name.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown Accion: $value")
        }
    }


    override fun toString(): String {
        return name.lowercase()
    }
}