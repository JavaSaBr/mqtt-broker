package javasabr.mqtt.network.message.in

import javasabr.mqtt.model.MqttMessageProperty
import javasabr.mqtt.model.MqttProperties
import javasabr.mqtt.model.QoS
import javasabr.mqtt.model.data.type.StringPair
import javasabr.rlib.collections.array.Array
import javasabr.rlib.collections.array.IntArray
import javasabr.rlib.common.util.ArrayUtils
import javasabr.rlib.common.util.BufferUtils

class PublishMqttInMessageTest extends BaseMqttInMessageTest {

  def "should read packet correctly as mqtt 3.1.1"() {
    given:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putString(publishTopic.toString())
          it.putShort(packetId)
          it.put(publishPayload)
        }
    when:
        def packet = new PublishMqttInMessage(0b0110_0011 as byte)
        def result = packet.read(defaultMqtt311Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        packet.qos == QoS.AT_LEAST_ONCE
        !packet.duplicate
        packet.retained
        packet.rawResponseTopicName == ""
        packet.subscriptionIds == IntArray.empty()
        packet.contentType == ""
        packet.correlationData == ArrayUtils.EMPTY_BYTE_ARRAY
        packet.payload == publishPayload
        packet.messageId == packetId
        packet.userProperties() == Array.empty()
        packet.messageExpiryInterval == MqttProperties.MESSAGE_EXPIRY_INTERVAL_UNDEFINED
        packet.topicAlias == MqttProperties.TOPIC_ALIAS_UNDEFINED
        packet.payloadFormat == MqttProperties.PAYLOAD_FORMAT_INDICATOR_DEFAULT
  }

  def "should read packet correctly as mqtt 5.0"() {
    given:
        def propertiesBuffer = BufferUtils.prepareBuffer(512) {
          it.putProperty(MqttMessageProperty.PAYLOAD_FORMAT_INDICATOR, 1)
          it.putProperty(MqttMessageProperty.MESSAGE_EXPIRY_INTERVAL, messageExpiryInterval)
          it.putProperty(MqttMessageProperty.TOPIC_ALIAS, topicAlias)
          it.putProperty(MqttMessageProperty.RESPONSE_TOPIC, responseTopic)
          it.putProperty(MqttMessageProperty.CORRELATION_DATA, correlationData)
          it.putProperty(MqttMessageProperty.USER_PROPERTY, userProperties)
          it.putProperty(MqttMessageProperty.SUBSCRIPTION_IDENTIFIER, subscriptionIds)
          it.putProperty(MqttMessageProperty.CONTENT_TYPE, contentType)
        }
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putString(publishTopic.toString())
          it.putShort(packetId)
          it.putMbi(propertiesBuffer.limit())
          it.put(propertiesBuffer)
          it.put(publishPayload)
        }
    when:
        def packet = new PublishMqttInMessage(0b0110_0011 as byte)
        def result = packet.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        packet.qos == QoS.AT_LEAST_ONCE
        !packet.duplicate
        packet.retained
        packet.rawResponseTopicName == responseTopic
        packet.subscriptionIds == subscriptionIds
        packet.contentType == contentType
        packet.correlationData == correlationData
        packet.payload == publishPayload
        packet.messageId == packetId
        packet.userProperties() == userProperties
        packet.messageExpiryInterval == messageExpiryInterval
        packet.topicAlias == topicAlias
        packet.payloadFormat
    when:
        dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putString(publishTopic.toString())
          it.putShort(packetId)
          it.putMbi(0)
          it.put(publishPayload)
        }
        packet = new PublishMqttInMessage(0b0110_0011 as byte)
        result = packet.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        packet.qos == QoS.AT_LEAST_ONCE
        !packet.duplicate
        packet.retained
        packet.rawResponseTopicName == ""
        packet.subscriptionIds == IntArray.empty()
        packet.contentType == ""
        packet.correlationData == ArrayUtils.EMPTY_BYTE_ARRAY
        packet.payload == publishPayload
        packet.messageId == packetId
        packet.userProperties() == Array.empty(StringPair)
        packet.messageExpiryInterval == MqttProperties.MESSAGE_EXPIRY_INTERVAL_UNDEFINED
        packet.topicAlias == MqttProperties.TOPIC_ALIAS_UNDEFINED
        packet.payloadFormat == MqttProperties.PAYLOAD_FORMAT_INDICATOR_DEFAULT
  }
}
