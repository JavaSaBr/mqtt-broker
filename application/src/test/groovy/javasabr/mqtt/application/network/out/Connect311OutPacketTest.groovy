package javasabr.mqtt.application.network.out

import javasabr.mqtt.network.packet.in.ConnectInPacket
import javasabr.mqtt.network.packet.out.Connect311OutPacket
import javasabr.mqtt.model.QoS
import javasabr.rlib.collections.array.Array
import javasabr.rlib.common.util.ArrayUtils
import javasabr.rlib.common.util.BufferUtils

class Connect311OutPacketTest extends BaseOutPacketTest {

  def "should write packet correctly"() {
    given:
        def packet = new Connect311OutPacket(
            userName,
            "",
            clientId,
            userPassword,
            ArrayUtils.EMPTY_BYTE_ARRAY,
            QoS.AT_MOST_ONCE,
            keepAlive,
            willRetain,
            cleanStart)
    when:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          packet.write(defaultMqtt311Connection, it)
        }
        def reader = new ConnectInPacket(0b0001_0000 as byte)
        def result = reader.read(defaultMqtt311Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        reader.username() == userName
        reader.clientId() == clientId
        reader.password() == userPassword
        reader.keepAlive() == keepAlive
        reader.userProperties() == Array.empty()
        reader.cleanStart() == cleanStart
        reader.willRetain  == willRetain
  }
}
