package javasabr.mqtt.broker.application.service

import javasabr.mqtt.auth.api.MqttCredentials
import javasabr.mqtt.auth.api.AuthenticationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.TestPropertySource

import java.nio.charset.StandardCharsets

@TestPropertySource(properties = [
    "authentication.credentials-source.file.enabled=true",
    "authentication.method.basic.enabled=true"
])
class FileAuthenticationServiceTest extends IntegrationSpecification {

  @Autowired
  AuthenticationService authenticationService

  def "should authenticate credentials according [credentials/test] file"() {
    given:
        def passwordBytes = password.getBytes(StandardCharsets.UTF_8)
        def request = new MqttCredentials(userName, passwordBytes, null, new byte[0])
    when:
        def result = authenticationService.authenticate(request).block()
    then:
        result == expectedResult
    where:
        userName | password           | expectedResult
        "user"   | "wrong-password"   | false
        "user"   | "correct-password" | true
        ""       | "correct-password" | false
        "user"   | ""                 | false
  }
}
