package javasabr.mqtt.network.message.out

import javasabr.mqtt.model.QoS
import javasabr.mqtt.network.message.in.ConnectMqttInMessage
import javasabr.rlib.collections.array.Array
import javasabr.rlib.common.util.ArrayUtils
import javasabr.rlib.common.util.BufferUtils

class ConnectMqtt311OutMessageTest extends BaseMqttOutMessageTest {

  def "should write packet correctly"() {
    given:
        def packet = new ConnectMqtt311OutMessage(
            userName,
            "",
            mqtt311ClientId,
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
        def reader = new ConnectMqttInMessage(0b0001_0000 as byte)
        def result = reader.read(defaultMqtt311Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        reader.username() == userName
        reader.clientId() == mqtt311ClientId
        reader.password() == userPassword
        reader.keepAlive() == keepAlive
        reader.userProperties() == Array.empty()
        reader.cleanStart() == cleanStart
        reader.willRetain  == willRetain
  }
}
