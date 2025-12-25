package javasabr.mqtt.broker.application.service

import javasabr.mqtt.auth.service.config.AuthenticationServiceSpringConfig
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import spock.lang.Specification

@TestPropertySource("classpath:application-test.properties")
@SpringJUnitConfig(classes = AuthenticationServiceSpringConfig)
class IntegrationSpecification extends Specification {
}
