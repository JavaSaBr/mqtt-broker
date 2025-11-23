package javasabr.mqtt.network.message.in

import javasabr.mqtt.model.MqttMessageProperty
import javasabr.mqtt.model.MqttProperties
import javasabr.mqtt.model.PayloadFormat
import javasabr.mqtt.model.QoS
import javasabr.mqtt.model.exception.MalformedProtocolMqttException
import javasabr.mqtt.model.message.MqttMessageType
import javasabr.rlib.collections.array.IntArray
import javasabr.rlib.common.util.BufferUtils

import java.nio.charset.StandardCharsets

class PublishMqttInMessageTest extends BaseMqttInMessageTest {

  def "should read message correctly as MQTT 3.1.1"() {
    given:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putString(publishTopic.toString())
          it.putShort(messageId)
          it.put(publishPayload)
        }
    when:
        def inMessage = new PublishMqttInMessage(0b0110_0011 as byte)
        def result = inMessage.read(defaultMqtt311Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        inMessage.qos() == QoS.AT_LEAST_ONCE
        !inMessage.duplicate()
        inMessage.retain()
        inMessage.rawResponseTopicName() == null
        inMessage.subscriptionIds() == IntArray.empty()
        inMessage.contentType() == null
        inMessage.correlationData() == null
        inMessage.payload() == publishPayload
        inMessage.messageId() == messageId
        inMessage.userProperties() == MqttInMessage.EMPTY_USER_PROPERTIES
        inMessage.messageExpiryInterval() == MqttProperties.MESSAGE_EXPIRY_INTERVAL_IS_NOT_SET
        inMessage.topicAlias() == MqttProperties.TOPIC_ALIAS_NOT_SET
        inMessage.payloadFormat() == PayloadFormat.UNDEFINED
  }

  def "should read message correctly as MQTT 5.0"() {
    given:
        def propertiesBuffer = BufferUtils.prepareBuffer(512) {
          it.putProperty(MqttMessageProperty.PAYLOAD_FORMAT_INDICATOR, 1)
          it.putProperty(MqttMessageProperty.MESSAGE_EXPIRY_INTERVAL, messageExpiryInterval)
          it.putProperty(MqttMessageProperty.TOPIC_ALIAS, topicAlias)
          it.putProperty(MqttMessageProperty.RESPONSE_TOPIC, responseTopic.rawTopic())
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
        message.rawResponseTopicName() == responseTopic.rawTopic()
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
        message.topicAlias() == MqttProperties.TOPIC_ALIAS_NOT_SET
        message.payloadFormat() == PayloadFormat.UNDEFINED
  }

  def "should not read invalid message as MQTT 5.0"() {
    given:
        def propertiesBuffer = BufferUtils.prepareBuffer(512) {
          it.putProperty(MqttMessageProperty.SERVER_KEEP_ALIVE, 1)
        }
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putString(publishTopic.toString())
          it.putShort(messageId)
          it.putMbi(propertiesBuffer.limit())
          it.put(propertiesBuffer)
          it.put(publishPayload)
        }
    when: 'use not available property'
        def inMessage = new PublishMqttInMessage(0b0110_0011 as byte)
        def successful = inMessage.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        !successful
        inMessage.exception() instanceof MalformedProtocolMqttException
        inMessage.exception().message == "Property:[$MqttMessageProperty.SERVER_KEEP_ALIVE] is not available for message:[$MqttMessageType.PUBLISH]"
    when: 'use 2 times topic alias'
        def propertiesBuffer2 = BufferUtils.prepareBuffer(512) {
          it.putProperty(MqttMessageProperty.TOPIC_ALIAS, 55)
          it.putProperty(MqttMessageProperty.TOPIC_ALIAS, 55)
        }
        def dataBuffer2 = BufferUtils.prepareBuffer(512) {
          it.putString(publishTopic.toString())
          it.putShort(messageId)
          it.putMbi(propertiesBuffer2.limit())
          it.put(propertiesBuffer2)
          it.put(publishPayload)
        }
        def inMessage2 = new PublishMqttInMessage(0b0110_0011 as byte)
        def successful2 = inMessage2.read(defaultMqtt5Connection, dataBuffer2, dataBuffer2.limit())
    then:
        !successful2
        inMessage2.exception() instanceof MalformedProtocolMqttException
        inMessage2.exception().message == "Property:[$MqttMessageProperty.TOPIC_ALIAS] is already presented in message:[$MqttMessageType.PUBLISH]"
    when: 'use 2 times response topic'
        def propertiesBuffer3 = BufferUtils.prepareBuffer(512) {
          it.putProperty(MqttMessageProperty.RESPONSE_TOPIC, "topic1")
          it.putProperty(MqttMessageProperty.RESPONSE_TOPIC, "topic1")
        }
        def dataBuffer3 = BufferUtils.prepareBuffer(512) {
          it.putString(publishTopic.toString())
          it.putShort(messageId)
          it.putMbi(propertiesBuffer3.limit())
          it.put(propertiesBuffer3)
          it.put(publishPayload)
        }
        def inMessage3 = new PublishMqttInMessage(0b0110_0011 as byte)
        def successful3 = inMessage3.read(defaultMqtt5Connection, dataBuffer3, dataBuffer3.limit())
    then:
        !successful3
        inMessage3.exception() instanceof MalformedProtocolMqttException
        inMessage3.exception().message == "Property:[$MqttMessageProperty.RESPONSE_TOPIC] is already presented in message:[$MqttMessageType.PUBLISH]"
    when: 'use 2 times content type'
        def propertiesBuffer4 = BufferUtils.prepareBuffer(512) {
          it.putProperty(MqttMessageProperty.CONTENT_TYPE, "json")
          it.putProperty(MqttMessageProperty.CONTENT_TYPE, "json")
        }
        def dataBuffer4 = BufferUtils.prepareBuffer(512) {
          it.putString(publishTopic.toString())
          it.putShort(messageId)
          it.putMbi(propertiesBuffer4.limit())
          it.put(propertiesBuffer4)
          it.put(publishPayload)
        }
        def inMessage4 = new PublishMqttInMessage(0b0110_0011 as byte)
        def successful4 = inMessage4.read(defaultMqtt5Connection, dataBuffer4, dataBuffer4.limit())
    then:
        !successful4
        inMessage4.exception() instanceof MalformedProtocolMqttException
        inMessage4.exception().message == "Property:[$MqttMessageProperty.CONTENT_TYPE] is already presented in message:[$MqttMessageType.PUBLISH]"
    when: 'use 2 times correlation data'
        def propertiesBuffer5 = BufferUtils.prepareBuffer(512) {
          it.putProperty(MqttMessageProperty.CORRELATION_DATA, "data".getBytes(StandardCharsets.UTF_8))
          it.putProperty(MqttMessageProperty.CORRELATION_DATA, "data".getBytes(StandardCharsets.UTF_8))
        }
        def dataBuffer5 = BufferUtils.prepareBuffer(512) {
          it.putString(publishTopic.toString())
          it.putShort(messageId)
          it.putMbi(propertiesBuffer5.limit())
          it.put(propertiesBuffer5)
          it.put(publishPayload)
        }
        def inMessage5 = new PublishMqttInMessage(0b0110_0011 as byte)
        def successful5 = inMessage5.read(defaultMqtt5Connection, dataBuffer5, dataBuffer5.limit())
    then:
        !successful5
        inMessage5.exception() instanceof MalformedProtocolMqttException
        inMessage5.exception().message == "Property:[$MqttMessageProperty.CORRELATION_DATA] is already presented in message:[$MqttMessageType.PUBLISH]"
  }
}
