package javasabr.mqtt.network.message.in

import javasabr.mqtt.model.MqttMessageProperty
import javasabr.mqtt.model.reason.code.DisconnectReasonCode
import javasabr.rlib.common.util.BufferUtils

class DisconnectMqttInMessageTest extends BaseMqttInMessageTest {

  def 'should read message correctly as mqtt 5.0'() {
    given:
        def propertiesBuffer = BufferUtils.prepareBuffer(512) {
          it.putProperty(MqttMessageProperty.SESSION_EXPIRY_INTERVAL, sessionExpiryInterval)
          it.putProperty(MqttMessageProperty.REASON_STRING, reasonString)
          it.putProperty(MqttMessageProperty.SERVER_REFERENCE, serverReference)
          it.putProperty(MqttMessageProperty.USER_PROPERTY, userProperties)
        }
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putByte(DisconnectReasonCode.QUOTA_EXCEEDED.code())
          it.putMbi(propertiesBuffer.limit())
          it.put(propertiesBuffer)
        }
    when:
        def inMessage = new DisconnectMqttInMessage(0b1110_0000 as byte)
        def result = inMessage.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        inMessage.reason() == reasonString
        inMessage.serverReference() == serverReference
        inMessage.reasonCode() == DisconnectReasonCode.QUOTA_EXCEEDED
        inMessage.sessionExpiryInterval() == sessionExpiryInterval
        inMessage.userProperties() == userProperties
    when:
        propertiesBuffer = BufferUtils.prepareBuffer(512) {
          it.putProperty(MqttMessageProperty.SESSION_EXPIRY_INTERVAL, sessionExpiryInterval)
          it.putProperty(MqttMessageProperty.SERVER_REFERENCE, serverReference)
        }
        dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putByte(DisconnectReasonCode.PACKET_TOO_LARGE.code())
          it.putMbi(propertiesBuffer.limit())
          it.put(propertiesBuffer)
        }
        inMessage = new DisconnectMqttInMessage(0b1110_0000 as byte)
        result = inMessage.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        inMessage.reason() == ""
        inMessage.serverReference() == serverReference
        inMessage.reasonCode() == DisconnectReasonCode.PACKET_TOO_LARGE
        inMessage.sessionExpiryInterval() == sessionExpiryInterval
        inMessage.userProperties() == MqttInMessage.EMPTY_USER_PROPERTIES
  }
}
