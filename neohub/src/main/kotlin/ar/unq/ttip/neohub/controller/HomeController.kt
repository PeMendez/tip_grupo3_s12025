package ar.unq.ttip.neohub.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class HomeController {

    @GetMapping("/status")
    fun getStatus(): String {
        return "API is running!"
    }
}
