package javasabr.mqtt.broker.application.service


import javasabr.mqtt.auth.service.config.BasicAuthenticationSpringConfig
import javasabr.mqtt.auth.service.config.DatabaseSpringConfig
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import spock.lang.Specification

@TestPropertySource("classpath:application-test.properties")
@SpringJUnitConfig(classes = [
  BasicAuthenticationSpringConfig,
  DatabaseSpringConfig
])
class IntegrationSpecification extends Specification {
}
