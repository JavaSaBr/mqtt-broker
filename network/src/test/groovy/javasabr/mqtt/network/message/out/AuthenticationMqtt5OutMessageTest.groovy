package javasabr.mqtt.network.message.out

import javasabr.mqtt.model.message.MqttMessageType
import javasabr.mqtt.model.reason.code.AuthenticateReasonCode
import javasabr.mqtt.network.message.in.AuthenticationMqttInMessage
import javasabr.rlib.common.util.BufferUtils
import javasabr.rlib.common.util.NumberUtils

class AuthenticationMqtt5OutMessageTest extends BaseMqttOutMessageTest {

  def "should write message correctly"() {
    given:
        def outMessage = new AuthenticationMqtt5OutMessage(
            AuthenticateReasonCode.CONTINUE_AUTHENTICATION,
            reasonString,
            authMethod,
            authData,
            testUserProperties)
    when:
        def typeAndFlags = outMessage.messageTypeAndFlags()
        byte type = NumberUtils.getHighByteBits(typeAndFlags);
        byte info = NumberUtils.getLowByteBits(typeAndFlags);
    then:
        MqttMessageType.fromByte(type) == MqttMessageType.AUTHENTICATION
        info == AuthenticationMqttInMessage.MESSAGE_FLAGS
    when:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          outMessage.write(defaultMqtt5Connection, it)
        }
        def reader = new AuthenticationMqttInMessage(info)
        def result = reader.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        reader.reasonCode() == AuthenticateReasonCode.CONTINUE_AUTHENTICATION
        reader.reason() == reasonString
        reader.authenticationMethod() == authMethod
        reader.authenticationData() == authData
        reader.userProperties() == testUserProperties
  }
}
