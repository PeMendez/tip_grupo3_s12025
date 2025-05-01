package ar.unq.ttip.neohub.repository

import ar.unq.ttip.neohub.model.devices.SmartOutlet
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.TestPropertySource

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(locations = ["classpath:application.properties"])
class DeviceRepositoryTest {

    @Autowired
    lateinit var deviceRepository: DeviceRepository

    @Test
    fun `guardar y recuperar dispositivo`() {
        // Crear un dispositivo
        val device = SmartOutlet(name = "Lamp")
        val savedDevice = deviceRepository.save(device)

        // Recuperar el dispositivo
        val retrievedDevice = deviceRepository.findById(savedDevice.id).orElse(null)
        assertNotNull(retrievedDevice)
        assertEquals("Lamp", retrievedDevice?.name)
        assertEquals("smartOutlet", retrievedDevice?.type)
    }
}
