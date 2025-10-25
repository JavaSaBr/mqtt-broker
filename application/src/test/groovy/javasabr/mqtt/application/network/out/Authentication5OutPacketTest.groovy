package javasabr.mqtt.application.network.out

import javasabr.mqtt.model.reason.code.AuthenticateReasonCode
import javasabr.mqtt.network.message.in.AuthenticationMqttInMessage
import javasabr.mqtt.network.message.out.AuthenticationMqtt5OutMessage
import javasabr.rlib.common.util.BufferUtils

class Authentication5OutPacketTest extends BaseOutPacketTest {

  def "should write packet correctly"() {
    given:
        def packet = new AuthenticationMqtt5OutMessage(
            userProperties,
            AuthenticateReasonCode.CONTINUE_AUTHENTICATION,
            reasonString,
            authMethod,
            authData,)
    when:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          packet.write(defaultMqtt5Connection, it)
        }
        def reader = new AuthenticationMqttInMessage(0b1111_0000 as byte)
        def result = reader.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        reader.reasonCode == AuthenticateReasonCode.CONTINUE_AUTHENTICATION
        reader.authenticationMethod == authMethod
        reader.authenticationData == authData
        reader.reason == reasonString
        reader.userProperties() == userProperties
  }
}
