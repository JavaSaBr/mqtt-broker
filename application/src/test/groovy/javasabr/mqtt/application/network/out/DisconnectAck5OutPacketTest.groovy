package javasabr.mqtt.application.network.out

import javasabr.mqtt.model.reason.code.DisconnectReasonCode
import javasabr.mqtt.network.message.in.DisconnectMqttInMessage
import javasabr.mqtt.network.message.out.DisconnectMqtt5OutMessage
import javasabr.rlib.common.util.BufferUtils

class DisconnectAck5OutPacketTest extends BaseOutPacketTest {

  def "should write packet correctly"() {
    given:
        def packet = new DisconnectMqtt5OutMessage(
            DisconnectReasonCode.PACKET_TOO_LARGE,
            userProperties,
            reasonString,
            serverReference,
            sessionExpiryInterval)
    when:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          packet.write(defaultMqtt5Connection, it)
        }
        def reader = new DisconnectMqttInMessage(0b1110_0000 as byte)
        def result = reader.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        reader.reasonCode == DisconnectReasonCode.PACKET_TOO_LARGE
        reader.userProperties() == userProperties
        reader.reason == reasonString
        reader.serverReference == serverReference
        reader.sessionExpiryInterval == sessionExpiryInterval
  }
}
