package javasabr.mqtt.network.message.in

import javasabr.mqtt.model.MqttMessageProperty
import javasabr.mqtt.model.reason.code.AuthenticateReasonCode
import javasabr.rlib.common.util.BufferUtils

class AuthenticationMqttInMessageTest extends BaseMqttInMessageTest {

  def "should read packet correctly as mqtt 5.0"() {
    given:
        def propertiesBuffer = BufferUtils.prepareBuffer(512) {
          it.putProperty(MqttMessageProperty.AUTHENTICATION_METHOD, authMethod)
          it.putProperty(MqttMessageProperty.AUTHENTICATION_DATA, authData)
          it.putProperty(MqttMessageProperty.REASON_STRING, reasonString)
          it.putProperty(MqttMessageProperty.USER_PROPERTY, userProperties)
        }
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.put(AuthenticateReasonCode.SUCCESS.value)
          it.putMbi(propertiesBuffer.limit())
          it.put(propertiesBuffer)
        }
    when:
        def packet = new AuthenticationMqttInMessage(0b1111_0000 as byte)
        def result = packet.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        packet.reasonCode == AuthenticateReasonCode.SUCCESS
        packet.authenticationMethod == authMethod
        packet.authenticationData == authData
        packet.reason == reasonString
        packet.userProperties() == userProperties
    when:
        propertiesBuffer = BufferUtils.prepareBuffer(512) {
          it.putProperty(MqttMessageProperty.AUTHENTICATION_METHOD, authMethod)
          it.putProperty(MqttMessageProperty.REASON_STRING, reasonString)
          it.putProperty(MqttMessageProperty.USER_PROPERTY, userProperties)
          it.putProperty(MqttMessageProperty.AUTHENTICATION_DATA, authData)
        }
        dataBuffer = BufferUtils.prepareBuffer(512) {
          it.put(AuthenticateReasonCode.CONTINUE_AUTHENTICATION.value)
          it.putMbi(propertiesBuffer.limit())
          it.put(propertiesBuffer)
        }
        packet = new AuthenticationMqttInMessage(0b1111_0000 as byte)
        result = packet.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        packet.reasonCode == AuthenticateReasonCode.CONTINUE_AUTHENTICATION
        packet.authenticationMethod == authMethod
        packet.authenticationData == authData
        packet.reason == reasonString
        packet.userProperties() == userProperties
    when:
        propertiesBuffer = BufferUtils.prepareBuffer(512) {
          it.putProperty(MqttMessageProperty.AUTHENTICATION_METHOD, authMethod)
          it.putProperty(MqttMessageProperty.AUTHENTICATION_DATA, authData)
        }
        dataBuffer = BufferUtils.prepareBuffer(512) {
          it.put(AuthenticateReasonCode.CONTINUE_AUTHENTICATION.value)
          it.putMbi(propertiesBuffer.limit())
          it.put(propertiesBuffer)
        }
        packet = new AuthenticationMqttInMessage(0b1111_0000 as byte)
        result = packet.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        packet.reasonCode == AuthenticateReasonCode.CONTINUE_AUTHENTICATION
        packet.authenticationMethod == authMethod
        packet.authenticationData == authData
        packet.reason == ""
        packet.userProperties() == MqttInMessage.EMPTY_USER_PROPERTIES
  }
}
