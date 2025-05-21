package ar.unq.ttip.neohub.service

import org.springframework.context.ApplicationEvent

class UnconfiguredDeviceEvent(val message: String): ApplicationEvent(message)