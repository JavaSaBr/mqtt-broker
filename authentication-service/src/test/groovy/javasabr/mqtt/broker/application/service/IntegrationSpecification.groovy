package javasabr.mqtt.broker.application.service


import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import spock.lang.Specification

@TestPropertySource("classpath:application-test.properties")
@SpringJUnitConfig(classes = [
    DatabaseTestSpringConfig
])
class IntegrationSpecification extends Specification {
}
