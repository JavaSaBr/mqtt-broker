package javasabr.mqtt.network.message.out

import javasabr.mqtt.model.QoS
import javasabr.mqtt.network.message.in.ConnectMqttInMessage
import javasabr.rlib.common.util.ArrayUtils
import javasabr.rlib.common.util.BufferUtils

class ConnectMqtt311OutMessageTest extends BaseMqttOutMessageTest {

  def "should write packet correctly"() {
    given:
        def packet = new ConnectMqtt311OutMessage(
            testUserName,
            "",
            mqtt311ClientId,
            testUserPassword,
            ArrayUtils.EMPTY_BYTE_ARRAY,
            QoS.AT_MOST_ONCE,
            testKeepAlive,
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
        reader.username() == testUserName
        reader.clientId() == mqtt311ClientId
        reader.password() == testUserPassword
        reader.keepAlive() == testKeepAlive
        reader.userProperties() == MqttOutMessage.EMPTY_USER_PROPERTIES
        reader.cleanStart() == cleanStart
        reader.willRetain  == willRetain
  }
}
