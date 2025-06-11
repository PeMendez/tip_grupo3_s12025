package ar.unq.ttip.neohub.service

import ar.unq.ttip.neohub.dto.PushSubscriptionDto
import nl.martijndwars.webpush.Notification
import nl.martijndwars.webpush.PushService
import nl.martijndwars.webpush.Utils
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.security.Security

@Service
class PushNotificationService() {
    @Value("\${vapid.public.key}") lateinit var vapidPublicKey: String
    @Value("\${vapid.private.key}") lateinit var vapidPrivateKey: String
    //private val vapidPublicKey = environment.getProperty("VAPID_PUBLIC_KEY")
    //private val vapidPrivateKey = environment.getProperty("VAPID_PRIVATE_KEY")
    private val subscriptions = mutableListOf<PushSubscriptionDto>()

    init {
        Security.addProvider(BouncyCastleProvider())
    }
    fun getSubscriptions(): List<PushSubscriptionDto> = subscriptions
    fun addSubscription(subscription: PushSubscriptionDto) {
        subscriptions.add(subscription)
    }

    fun sendPushNotification(title: String, body: String) {
        val pushService = PushService()
        pushService.publicKey = Utils.loadPublicKey(vapidPublicKey)
        pushService.privateKey = Utils.loadPrivateKey(vapidPrivateKey)

        val payload = """{"title": "$title", "body": "$body"}"""
        val subs =  this.subscriptions

        if(subs.isNotEmpty()) {
            subs.forEach { subscription ->
                val notification = Notification(
                    subscription.endpoint,
                    subscription.keys.p256dh,
                    subscription.keys.auth,
                    payload.toByteArray()
                )
                try {
                    pushService.send(notification)
                    println("Notificación enviada con éxito a ${subscription.endpoint}")
                } catch (e: Exception) {
                    println("Error al enviar la notificación: ${e.message}")
                }
            }
        } else println("No se envio la push porque no hay suscripciones.")
    }
}
