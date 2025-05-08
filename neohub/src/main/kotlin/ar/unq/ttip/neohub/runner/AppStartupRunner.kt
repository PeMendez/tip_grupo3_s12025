package ar.unq.ttip.neohub.runner

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class AppStartupRunner(
    private val deviceTopicSubscriber: DeviceTopicSubscriber
): ApplicationRunner {
    override fun run(args: ApplicationArguments?) {
        println("Suscribiendose a todos los topics de los dispositivos...")
        deviceTopicSubscriber.subscribeToDeviceTopics()
        println("Suscripcion ready.")
    }
}