package javasabr.mqtt.broker.application

import com.hivemq.client.mqtt.mqtt3.exceptions.Mqtt3ConnAckException
import com.hivemq.client.mqtt.mqtt3.message.auth.Mqtt3SimpleAuth
import com.hivemq.client.mqtt.mqtt3.message.connect.Mqtt3Connect
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5ConnAckException
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5SimpleAuth
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect
import org.springframework.test.context.TestPropertySource

import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletionException

@TestPropertySource(properties = ["authentication.allow.anonymous=false"])
class AuthenticationTest extends IntegrationSpecification {

  def "should not be able to connect with wrong password using mqtt 3.1.1 client"() {
    given:
        def existingUsername = "user"
        def wrongPassword = "password1"
        def subscriber = buildExternalMqtt311Client()
        def connectMessage = Mqtt3Connect.builder()
            .simpleAuth(Mqtt3SimpleAuth.builder()
                .username(existingUsername)
                .password(wrongPassword.getBytes(StandardCharsets.UTF_8))
                .build())
            .build()
    when:
        subscriber.connect(connectMessage).join()
    then:
        def e = thrown(CompletionException.class)
        with(e.cause as Mqtt3ConnAckException) {
          message == "CONNECT failed as CONNACK contained an Error Code: BAD_USER_NAME_OR_PASSWORD."
        }
  }

  def "should be able to connect with correct password using mqtt 3.1.1 client"() {
    given:
        def existingUsername = "user"
        def correctPassword = "password"
    and:
        def subscriber = buildExternalMqtt311Client()
        def connectMessage = Mqtt3Connect.builder()
            .simpleAuth(Mqtt3SimpleAuth.builder()
                .username(existingUsername)
                .password(correctPassword.getBytes(StandardCharsets.UTF_8))
                .build())
            .build()
    when:
        subscriber.connect(connectMessage).join()
    then:
        noExceptionThrown()
    cleanup:
        subscriber.disconnect().join()
  }

  def "should not be able to connect with wrong password using mqtt 5 client"() {
    given:
        def existingUsername = "user"
        def wrongPassword = "password1"
    and:
        def subscriber = buildExternalMqtt5Client()
        def connectMessage = Mqtt5Connect.builder()
            .simpleAuth(Mqtt5SimpleAuth.builder()
                .username(existingUsername)
                .password(wrongPassword.getBytes(StandardCharsets.UTF_8))
                .build())
            .build()
    when:
        subscriber.connect(connectMessage).join()
    then:
        def e = thrown(CompletionException.class)
        with(e.cause as Mqtt5ConnAckException) {
          message == "CONNECT failed as CONNACK contained an Error Code: BAD_USER_NAME_OR_PASSWORD."
        }
  }

  def "should be able to connect with correct password using mqtt 5 client"() {
    given:
        def existingUsername = "user"
        def correctPassword = "password"
    and:
        def subscriber = buildExternalMqtt5Client()
        def connectMessage = Mqtt5Connect.builder()
            .simpleAuth(Mqtt5SimpleAuth.builder()
                .username(existingUsername)
                .password(correctPassword.getBytes(StandardCharsets.UTF_8))
                .build())
            .build()
    when:
        subscriber.connect(connectMessage).join()
    then:
        noExceptionThrown()
    cleanup:
        subscriber.disconnect().join()
  }
}
