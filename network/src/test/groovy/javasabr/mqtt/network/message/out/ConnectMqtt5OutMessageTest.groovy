package javasabr.mqtt.network.message.out

import javasabr.mqtt.model.QoS
import javasabr.mqtt.network.message.in.ConnectMqttInMessage
import javasabr.rlib.common.util.ArrayUtils
import javasabr.rlib.common.util.BufferUtils

class ConnectMqtt5OutMessageTest extends BaseMqttOutMessageTest {

  def "should write message correctly"() {
    given:
        def outMessage = new ConnectMqtt5OutMessage(
            testUserName,
            "",
            mqtt311ClientId,
            testUserPassword,
            ArrayUtils.EMPTY_BYTE_ARRAY,
            QoS.AT_MOST_ONCE,
            testKeepAlive,
            willRetain,
            cleanStart,
            testUserProperties,
            testAuthMethod,
            testAuthData,
            testSessionExpiryInterval,
            testReceiveMaxPublishes,
            testMaxMessageSize,
            testTopicAliasMaxValue,
            requestResponseInformation,
            requestProblemInformation)
    when:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          outMessage.write(defaultMqtt5Connection, it)
        }
        def reader = new ConnectMqttInMessage(0b0001_0000 as byte)
        def result = reader.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        reader.username() == testUserName
        reader.clientId() == mqtt311ClientId
        reader.password() == testUserPassword
        reader.keepAlive() == testKeepAlive
        reader.userProperties() == testUserProperties
        reader.cleanStart() == cleanStart
        reader.willRetain() == willRetain
        reader.authenticationMethod() == testAuthMethod
        reader.authenticationData() == testAuthData
        reader.sessionExpiryInterval() == testSessionExpiryInterval
        reader.receiveMaxPublishes() == testReceiveMaxPublishes
        reader.maxMessageSize() == testMaxMessageSize
        reader.topicAliasMaxValue() == testTopicAliasMaxValue
        reader.requestResponseInformation() == requestResponseInformation
        reader.requestProblemInformation() == requestProblemInformation
  }
}
