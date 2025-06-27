package ar.unq.ttip.neohub

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class NeohubApplication

fun main(args: Array<String>) {
	runApplication<NeohubApplication>(*args)
}
