package javasabr.mqtt.network.message.in

import javasabr.mqtt.model.MqttMessageProperty
import javasabr.mqtt.model.MqttProperties
import javasabr.mqtt.model.PayloadFormat
import javasabr.mqtt.model.QoS
import javasabr.mqtt.model.exception.MalformedProtocolMqttException
import javasabr.mqtt.model.message.MqttMessageType
import javasabr.rlib.collections.array.IntArray
import javasabr.rlib.common.util.BufferUtils

class PublishMqttInMessageTest extends BaseMqttInMessageTest {

  def "should read message correctly as MQTT 3.1.1"() {
    given:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putString(testPublishTopic.toString())
          it.putShort(testMessageId)
          it.put(testPublishPayloadBytes)
        }
    when:
        def inMessage = new PublishMqttInMessage(0b0110_0011 as byte)
        def result = inMessage.read(defaultMqtt311Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        with(inMessage) {
          qos() == QoS.AT_LEAST_ONCE
          !duplicate()
          retain()
          rawResponseTopicName() == null
          subscriptionIds() == IntArray.empty()
          contentType() == null
          correlationData() == null
          payload() == testPublishPayloadBytes
          messageId() == testMessageId
          userProperties() == MqttInMessage.EMPTY_USER_PROPERTIES
          messageExpiryInterval() == MqttProperties.MESSAGE_EXPIRY_INTERVAL_IS_NOT_SET
          topicAlias() == MqttProperties.TOPIC_ALIAS_NOT_SET
          payloadFormat() == PayloadFormat.UNDEFINED
        }
  }

  def "should read message correctly as MQTT 5.0"() {
    given:
        def propertiesBuffer = BufferUtils.prepareBuffer(512) {
          it.putProperty(MqttMessageProperty.PAYLOAD_FORMAT_INDICATOR, 1)
          it.putProperty(MqttMessageProperty.MESSAGE_EXPIRY_INTERVAL, testMessageExpiryInterval)
          it.putProperty(MqttMessageProperty.TOPIC_ALIAS, testTopicAlias)
          it.putProperty(MqttMessageProperty.RESPONSE_TOPIC, testResponseTopic.rawTopic())
          it.putProperty(MqttMessageProperty.CORRELATION_DATA, testCorrelationDataBytes)
          it.putProperty(MqttMessageProperty.USER_PROPERTY, testUserProperties)
          it.putProperty(MqttMessageProperty.SUBSCRIPTION_IDENTIFIER, testSubscriptionIds)
          it.putProperty(MqttMessageProperty.CONTENT_TYPE, testContentType)
        }
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putString(testPublishTopic.toString())
          it.putShort(testMessageId)
          it.putMbi(propertiesBuffer.limit())
          it.put(propertiesBuffer)
          it.put(testPublishPayloadBytes)
        }
    when:
        def inMessage = new PublishMqttInMessage(0b0110_0011 as byte)
        def result = inMessage.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        with(inMessage) {
          qos() == QoS.AT_LEAST_ONCE
          !duplicate()
          retain()
          rawResponseTopicName() == testResponseTopic.rawTopic()
          subscriptionIds() == testSubscriptionIds
          contentType() == testContentType
          correlationData() == testCorrelationDataBytes
          payload() == testPublishPayloadBytes
          messageId() == testMessageId
          userProperties() == testUserProperties
          messageExpiryInterval() == testMessageExpiryInterval
          topicAlias() == testTopicAlias
          payloadFormat() == PayloadFormat.UTF8_STRING
        }
    when:
        dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putString(testPublishTopic.toString())
          it.putShort(testMessageId)
          it.putMbi(0)
          it.put(testPublishPayloadBytes)
        }
        inMessage = new PublishMqttInMessage(0b0110_0011 as byte)
        result = inMessage.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        with(inMessage) {
          qos() == QoS.AT_LEAST_ONCE
          !duplicate()
          retain()
          rawResponseTopicName() == null
          subscriptionIds() == IntArray.empty()
          contentType() == null
          correlationData() == null
          payload() == testPublishPayloadBytes
          messageId() == testMessageId
          userProperties() == MqttInMessage.EMPTY_USER_PROPERTIES
          messageExpiryInterval() == MqttProperties.MESSAGE_EXPIRY_INTERVAL_IS_NOT_SET
          topicAlias() == MqttProperties.TOPIC_ALIAS_NOT_SET
          payloadFormat() == PayloadFormat.UNDEFINED
        }
  }

  def "should not allow to send unexpected property"() {
    given:
        def propertiesBuffer = BufferUtils.prepareBuffer(512) {
          it.putProperty(MqttMessageProperty.SERVER_KEEP_ALIVE, 1)
        }
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putString(testPublishTopic.toString())
          it.putShort(testMessageId)
          it.putMbi(propertiesBuffer.limit())
          it.put(propertiesBuffer)
          it.put(testPublishPayloadBytes)
        }
    when: 'use not available property'
        def inMessage = new PublishMqttInMessage(0b0110_0011 as byte)
        def successful = inMessage.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        !successful
        with(inMessage) {
          exception() instanceof MalformedProtocolMqttException
          exception().message == "Property:[$MqttMessageProperty.SERVER_KEEP_ALIVE] is not available for message:[$MqttMessageType.PUBLISH]"
        }
  }

  def "should not allow duplicated properties in message"(MqttMessageProperty property, Object value) {
    given:
        def propertiesBuffer = BufferUtils.prepareBuffer(512) {
          it.putProperty(property, value)
          it.putProperty(property, value)
        }
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putString(testPublishTopic.toString())
          it.putShort(testMessageId)
          it.putMbi(propertiesBuffer.limit())
          it.put(propertiesBuffer)
        }
    when:
        def inMessage = new PublishMqttInMessage(0b0110_0011 as byte)
        def result = inMessage.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        !result
        with(inMessage) {
          exception() instanceof MalformedProtocolMqttException
          exception().message == "Property:[$property] is already presented in message:[$MqttMessageType.PUBLISH]"
        }
    where:
        property                             | value
        MqttMessageProperty.TOPIC_ALIAS      | testTopicAlias
        MqttMessageProperty.RESPONSE_TOPIC   | testResponseTopic.rawTopic()
        MqttMessageProperty.CONTENT_TYPE     | testContentType
        MqttMessageProperty.CORRELATION_DATA | testCorrelationDataBytes
  }
}
