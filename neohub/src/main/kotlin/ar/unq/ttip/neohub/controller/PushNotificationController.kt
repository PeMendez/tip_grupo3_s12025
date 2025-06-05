package ar.unq.ttip.neohub.controller

import ar.unq.ttip.neohub.dto.PushSubscriptionDto
import ar.unq.ttip.neohub.service.PushNotificationService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/push-subscribe")
class PushNotificationController(
    private val pushNotificationService: PushNotificationService
) {
    @PostMapping
    fun subscribe(@RequestBody subscription: PushSubscriptionDto): String {
        pushNotificationService.addSubscription(subscription)//subscriptions.add(subscription)
        println("Nueva suscripción: $subscription")
        return "Suscripción guardada exitosamente"
    }
}