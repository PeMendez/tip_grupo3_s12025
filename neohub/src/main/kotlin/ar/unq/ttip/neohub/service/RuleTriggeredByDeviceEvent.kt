package ar.unq.ttip.neohub.service

import ar.unq.ttip.neohub.model.Device
import org.springframework.context.ApplicationEvent

class RuleTriggeredByDeviceEvent (val device: Device): ApplicationEvent(device)
