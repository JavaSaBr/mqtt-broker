package javasabr.mqtt.application.network.out

import javasabr.mqtt.model.QoS
import javasabr.mqtt.network.packet.in.ConnectInPacket
import javasabr.mqtt.network.packet.out.Connect5OutPacket
import javasabr.rlib.common.util.ArrayUtils
import javasabr.rlib.common.util.BufferUtils

class Connect5OutPacketTest extends BaseOutPacketTest {

  def "should write packet correctly"() {
    given:
        def packet = new Connect5OutPacket(
            userName,
            "",
            mqtt311ClientId,
            userPassword,
            ArrayUtils.EMPTY_BYTE_ARRAY,
            QoS.AT_MOST_ONCE,
            keepAlive,
            willRetain,
            cleanStart,
            userProperties,
            authMethod,
            authData,
            sessionExpiryInterval,
            receiveMaxPublishes,
            maxPacketSize,
            topicAliasMaxValue,
            requestResponseInformation,
            requestProblemInformation)
    when:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          packet.write(defaultMqtt5Connection, it)
        }
        def reader = new ConnectInPacket(0b0001_0000 as byte)
        def result = reader.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        reader.username() == userName
        reader.clientId() == mqtt311ClientId
        reader.password() == userPassword
        reader.keepAlive() == keepAlive
        reader.userProperties() == userProperties
        reader.cleanStart() == cleanStart
        reader.willRetain() == willRetain
        reader.authenticationMethod() == authMethod
        reader.authenticationData() == authData
        reader.sessionExpiryInterval() == sessionExpiryInterval
        reader.receiveMaxPublishes() == receiveMaxPublishes
        reader.maxPacketSize() == maxPacketSize
        reader.topicAliasMaxValue() == topicAliasMaxValue
        reader.requestResponseInformation() == requestResponseInformation
        reader.requestProblemInformation() == requestProblemInformation
  }
}
