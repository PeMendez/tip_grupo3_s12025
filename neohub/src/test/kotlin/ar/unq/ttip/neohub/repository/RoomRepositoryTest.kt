package ar.unq.ttip.neohub.repository

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles

@DataJpaTest
@ActiveProfiles("test")
class RoomRepositoryTest {

    @Autowired
    lateinit var roomRepository: RoomRepository

    @Test
    fun `guardar y recuperar cuarto`() {

    }
}
