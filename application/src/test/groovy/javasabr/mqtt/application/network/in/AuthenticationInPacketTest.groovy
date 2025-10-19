package javasabr.mqtt.application.network.in

import javasabr.mqtt.network.packet.in.AuthenticationInPacket
import javasabr.mqtt.model.PacketProperty
import javasabr.mqtt.model.reason.code.AuthenticateReasonCode
import javasabr.rlib.collections.array.Array
import javasabr.rlib.common.util.BufferUtils

class AuthenticationInPacketTest extends BaseInPacketTest {

  def "should read packet correctly as mqtt 5.0"() {
    given:
        def propertiesBuffer = BufferUtils.prepareBuffer(512) {
          it.putProperty(PacketProperty.AUTHENTICATION_METHOD, authMethod)
          it.putProperty(PacketProperty.AUTHENTICATION_DATA, authData)
          it.putProperty(PacketProperty.REASON_STRING, reasonString)
          it.putProperty(PacketProperty.USER_PROPERTY, userProperties)
        }
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.put(AuthenticateReasonCode.SUCCESS.value)
          it.putMbi(propertiesBuffer.limit())
          it.put(propertiesBuffer)
        }
    when:
        def packet = new AuthenticationInPacket(0b1111_0000 as byte)
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
          it.putProperty(PacketProperty.AUTHENTICATION_METHOD, authMethod)
          it.putProperty(PacketProperty.REASON_STRING, reasonString)
          it.putProperty(PacketProperty.USER_PROPERTY, userProperties)
          it.putProperty(PacketProperty.AUTHENTICATION_DATA, authData)
        }
        dataBuffer = BufferUtils.prepareBuffer(512) {
          it.put(AuthenticateReasonCode.CONTINUE_AUTHENTICATION.value)
          it.putMbi(propertiesBuffer.limit())
          it.put(propertiesBuffer)
        }
        packet = new AuthenticationInPacket(0b1111_0000 as byte)
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
          it.putProperty(PacketProperty.AUTHENTICATION_METHOD, authMethod)
          it.putProperty(PacketProperty.AUTHENTICATION_DATA, authData)
        }
        dataBuffer = BufferUtils.prepareBuffer(512) {
          it.put(AuthenticateReasonCode.CONTINUE_AUTHENTICATION.value)
          it.putMbi(propertiesBuffer.limit())
          it.put(propertiesBuffer)
        }
        packet = new AuthenticationInPacket(0b1111_0000 as byte)
        result = packet.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        packet.reasonCode == AuthenticateReasonCode.CONTINUE_AUTHENTICATION
        packet.authenticationMethod == authMethod
        packet.authenticationData == authData
        packet.reason == ""
        packet.userProperties() == Array.empty()
  }
}
