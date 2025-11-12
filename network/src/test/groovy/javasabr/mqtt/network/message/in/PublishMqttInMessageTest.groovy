package javasabr.mqtt.network.message.in

import javasabr.mqtt.model.MqttMessageProperty
import javasabr.mqtt.model.MqttProperties
import javasabr.mqtt.model.PayloadFormat
import javasabr.mqtt.model.QoS
import javasabr.rlib.collections.array.IntArray
import javasabr.rlib.common.util.BufferUtils

class PublishMqttInMessageTest extends BaseMqttInMessageTest {

  def "should read message correctly as mqtt 3.1.1"() {
    given:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putString(publishTopic.toString())
          it.putShort(messageId)
          it.put(publishPayload)
        }
    when:
        def message = new PublishMqttInMessage(0b0110_0011 as byte)
        def result = message.read(defaultMqtt311Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        message.qos() == QoS.AT_LEAST_ONCE
        !message.duplicate()
        message.retain()
        message.rawResponseTopicName() == null
        message.subscriptionIds() == IntArray.empty()
        message.contentType() == null
        message.correlationData() == null
        message.payload() == publishPayload
        message.messageId() == messageId
        message.userProperties() == MqttInMessage.EMPTY_USER_PROPERTIES
        message.messageExpiryInterval() == MqttProperties.MESSAGE_EXPIRY_INTERVAL_IS_NOT_SET
        message.topicAlias() == MqttProperties.TOPIC_ALIAS_UNDEFINED
        message.payloadFormat() == PayloadFormat.UNDEFINED
  }

  def "should read message correctly as mqtt 5.0"() {
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
          it.putShort(messageId)
          it.putMbi(propertiesBuffer.limit())
          it.put(propertiesBuffer)
          it.put(publishPayload)
        }
    when:
        def message = new PublishMqttInMessage(0b0110_0011 as byte)
        def result = message.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        message.qos() == QoS.AT_LEAST_ONCE
        !message.duplicate()
        message.retain()
        message.rawResponseTopicName() == responseTopic
        message.subscriptionIds() == subscriptionIds
        message.contentType() == contentType
        message.correlationData() == correlationData
        message.payload() == publishPayload
        message.messageId() == messageId
        message.userProperties() == userProperties
        message.messageExpiryInterval() == messageExpiryInterval
        message.topicAlias() == topicAlias
        message.payloadFormat() == PayloadFormat.UTF8_STRING
    when:
        dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putString(publishTopic.toString())
          it.putShort(messageId)
          it.putMbi(0)
          it.put(publishPayload)
        }
        message = new PublishMqttInMessage(0b0110_0011 as byte)
        result = message.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        message.qos() == QoS.AT_LEAST_ONCE
        !message.duplicate()
        message.retain()
        message.rawResponseTopicName() == null
        message.subscriptionIds() == IntArray.empty()
        message.contentType() == null
        message.correlationData() == null
        message.payload() == publishPayload
        message.messageId() == messageId
        message.userProperties() == MqttInMessage.EMPTY_USER_PROPERTIES
        message.messageExpiryInterval() == MqttProperties.MESSAGE_EXPIRY_INTERVAL_IS_NOT_SET
        message.topicAlias() == MqttProperties.TOPIC_ALIAS_UNDEFINED
        message.payloadFormat() == PayloadFormat.UNDEFINED
  }
}
