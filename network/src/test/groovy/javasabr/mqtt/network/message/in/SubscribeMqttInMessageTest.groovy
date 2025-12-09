package javasabr.mqtt.network.message.in

import javasabr.mqtt.model.MqttMessageProperty
import javasabr.mqtt.model.MqttProperties
import javasabr.mqtt.model.MqttProtocolErrors
import javasabr.mqtt.model.QoS
import javasabr.mqtt.model.SubscribeRetainHandling
import javasabr.mqtt.model.exception.MalformedProtocolMqttException
import javasabr.mqtt.model.message.MqttMessageType
import javasabr.rlib.common.util.BufferUtils

class SubscribeMqttInMessageTest extends BaseMqttInMessageTest {

  def "should read message correctly as MQTT 3.1.1"() {
    given:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(testMessageId)
          it.putString(topicFilter)
          it.put(0b0000_0001 as byte) // QoS.AT_LEAST_ONCE
          it.putString(topicFilter2)
          it.put(0b0000_0010 as byte) // QoS.EXACTLY_ONCE
        }
    when:
        def inMessage = new SubscribeMqttInMessage(SubscribeMqttInMessage.MESSAGE_FLAGS)
        def successful = inMessage.read(defaultMqtt311Connection, dataBuffer, dataBuffer.limit())
    then:
        successful
        def subscriptions = inMessage.subscriptions()
        subscriptions.size() == 2
        subscriptions.get(0).qos() == QoS.AT_LEAST_ONCE
        subscriptions.get(0).rawTopicFilter() == topicFilter
        subscriptions.get(0).noLocal()
        subscriptions.get(0).retainAsPublished()
        subscriptions.get(0).retainHandling() == SubscribeRetainHandling.SEND
        subscriptions.get(1).qos() == QoS.EXACTLY_ONCE
        subscriptions.get(1).rawTopicFilter().toString() == topicFilter2
        subscriptions.get(1).noLocal()
        subscriptions.get(1).retainAsPublished()
        subscriptions.get(1).retainHandling() == SubscribeRetainHandling.SEND
        inMessage.messageId() == testMessageId
        inMessage.userProperties() == MqttInMessage.EMPTY_USER_PROPERTIES
        inMessage.subscriptionId() == MqttProperties.SUBSCRIPTION_ID_IS_NOT_SET
  }

  def "should read message correctly as MQTT 5.0"() {
    given:
        def propertiesBuffer = BufferUtils.prepareBuffer(512) {
          it.putProperty(MqttMessageProperty.SUBSCRIPTION_IDENTIFIER, subscriptionId)
          it.putProperty(MqttMessageProperty.USER_PROPERTY, testUserProperties)
        }
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(testMessageId)
          it.putMbi(propertiesBuffer.limit())
          it.put(propertiesBuffer)
          it.putString(topicFilter)
          it.put(0b0000_1001 as byte)  // QoS.AT_LEAST_ONCE, with local, retainAsPublished, SubscribeRetainHandling.SEND
          it.putString(topicFilter2)
          it.put(0b0001_0110 as byte)  // QoS.EXACTLY_ONCE, no local, no retainAsPublished, SubscribeRetainHandling.SEND_IF_SUBSCRIPTION_DOES_NOT_EXIST
          it.putString(topicFilter3)
          it.put(0b0010_0100 as byte)  // QoS.AT_MOST_ONCE, no local, retainAsPublished, SubscribeRetainHandling.DO_NOT_SEND
        }
    when:
        def inMessage = new SubscribeMqttInMessage(SubscribeMqttInMessage.MESSAGE_FLAGS)
        def successful = inMessage.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
        def subscriptions = inMessage.subscriptions()
    then:
        successful
        subscriptions.size() == 3
        subscriptions.get(0).qos() == QoS.AT_LEAST_ONCE
        subscriptions.get(0).rawTopicFilter() == topicFilter
        !subscriptions.get(0).noLocal()
        subscriptions.get(0).retainAsPublished()
        subscriptions.get(0).retainHandling() == SubscribeRetainHandling.SEND
        subscriptions.get(1).qos() == QoS.EXACTLY_ONCE
        subscriptions.get(1).rawTopicFilter() == topicFilter2
        subscriptions.get(1).noLocal()
        !subscriptions.get(1).retainAsPublished()
        subscriptions.get(1).retainHandling() == SubscribeRetainHandling.SEND_IF_SUBSCRIPTION_DOES_NOT_EXIST
        subscriptions.get(2).qos() == QoS.AT_MOST_ONCE
        subscriptions.get(2).rawTopicFilter() == topicFilter3
        subscriptions.get(2).noLocal()
        !subscriptions.get(2).retainAsPublished()
        subscriptions.get(2).retainHandling() == SubscribeRetainHandling.DO_NOT_SEND
        inMessage.messageId() == testMessageId
        inMessage.userProperties() == testUserProperties
        inMessage.subscriptionId() == subscriptionId
    when:
        dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(testMessageId)
          it.putMbi(0)
          it.putString(topicFilter)
          it.put(0b0000_0001 as byte)
          it.putString(topicFilter2)
          it.put(0b0000_0010 as byte)
        }
        inMessage = new SubscribeMqttInMessage(SubscribeMqttInMessage.MESSAGE_FLAGS)
        successful = inMessage.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
        subscriptions = inMessage.subscriptions()
    then:
        successful
        subscriptions.size() == 2
        subscriptions.get(0).qos() == QoS.AT_LEAST_ONCE
        subscriptions.get(0).rawTopicFilter() == topicFilter
        !subscriptions.get(0).noLocal()
        !subscriptions.get(0).retainAsPublished()
        subscriptions.get(0).retainHandling() == SubscribeRetainHandling.SEND
        subscriptions.get(1).qos() == QoS.EXACTLY_ONCE
        subscriptions.get(1).rawTopicFilter() == topicFilter2
        !subscriptions.get(1).noLocal()
        !subscriptions.get(1).retainAsPublished()
        subscriptions.get(1).retainHandling() == SubscribeRetainHandling.SEND
        inMessage.messageId() == testMessageId
        inMessage.userProperties() == MqttInMessage.EMPTY_USER_PROPERTIES
        inMessage.subscriptionId() == MqttProperties.SUBSCRIPTION_ID_IS_NOT_SET
  }

  def "should not read invalid message as MQTT 3.1.1"() {
    given:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(testMessageId)
          it.putString(topicFilter)
          it.put(0b0000_0001 as byte)
        }
    when: 'invalid message flags'
        def inMessage = new SubscribeMqttInMessage(0b0000_0000 as byte)
        def successful = inMessage.read(defaultMqtt311Connection, dataBuffer, dataBuffer.limit())
    then:
        !successful
        inMessage.exception() instanceof MalformedProtocolMqttException
        inMessage.exception().message == "Unexpected message flags:[0b0000_0000] in message:[$MqttMessageType.SUBSCRIBE]"
    when: 'invalid QoS or Retain Handling'
        def dataBuffer2 = BufferUtils.prepareBuffer(512) {
          it.putShort(testMessageId)
          it.putString(topicFilter)
          it.put(0b0000_0011 as byte)
        }
        def inMessage2 = new SubscribeMqttInMessage(SubscribeMqttInMessage.MESSAGE_FLAGS)
        def successful2 = inMessage2.read(defaultMqtt311Connection, dataBuffer2, dataBuffer2.limit())
    then:
        !successful2
        inMessage2.exception() instanceof MalformedProtocolMqttException
        inMessage2.exception().message == MqttProtocolErrors.UNSUPPORTED_QOS_OR_RETAIN_HANDLING
    when: 'not provided any topic filter'
        def dataBuffer3 = BufferUtils.prepareBuffer(512) {
          it.putShort(testMessageId)
        }
        def inMessage3 = new SubscribeMqttInMessage(SubscribeMqttInMessage.MESSAGE_FLAGS)
        def successful3 = inMessage3.read(defaultMqtt311Connection, dataBuffer3, dataBuffer3.limit())
    then:
        !successful3
        inMessage3.exception() instanceof MalformedProtocolMqttException
        inMessage3.exception().message == MqttProtocolErrors.NO_ANY_TOPIC_FILTERS
    when: 'unsupported no local option'
        def dataBuffer4 = BufferUtils.prepareBuffer(512) {
          it.putShort(testMessageId)
          it.putString(topicFilter)
          it.put(0b0000_0100 as byte)
        }
        def inMessage4 = new SubscribeMqttInMessage(SubscribeMqttInMessage.MESSAGE_FLAGS)
        def successful4 = inMessage4.read(defaultMqtt311Connection, dataBuffer4, dataBuffer4.limit())
    then:
        !successful4
        inMessage4.exception() instanceof MalformedProtocolMqttException
        inMessage4.exception().message == MqttProtocolErrors.PROTOCOL_LEVEL_UNSUPPORTED_NO_LOCAL_OPTION
    when: 'unsupported retain as publish option'
        def dataBuffer5 = BufferUtils.prepareBuffer(512) {
          it.putShort(testMessageId)
          it.putString(topicFilter)
          it.put(0b0000_1000 as byte)
        }
        def inMessage5 = new SubscribeMqttInMessage(SubscribeMqttInMessage.MESSAGE_FLAGS)
        def successful5 = inMessage5.read(defaultMqtt311Connection, dataBuffer5, dataBuffer5.limit())
    then:
        !successful5
        inMessage5.exception() instanceof MalformedProtocolMqttException
        inMessage5.exception().message == MqttProtocolErrors.PROTOCOL_LEVEL_UNSUPPORTED_RETAIN_AS_PUBLISH_OPTION
    when: 'unsupported retain handling option'
        def dataBuffer6 = BufferUtils.prepareBuffer(512) {
          it.putShort(testMessageId)
          it.putString(topicFilter)
          it.put(0b0011_0000 as byte)
        }
        def inMessage6 = new SubscribeMqttInMessage(SubscribeMqttInMessage.MESSAGE_FLAGS)
        def successful6 = inMessage6.read(defaultMqtt311Connection, dataBuffer6, dataBuffer6.limit())
    then:
        !successful6
        inMessage6.exception() instanceof MalformedProtocolMqttException
        inMessage6.exception().message == MqttProtocolErrors.PROTOCOL_LEVEL_UNSUPPORTED_RETAIN_HANDLING_OPTION
  }

  def "should not read invalid message as MQTT 5.0"() {
    given:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(testMessageId)
          it.putMbi(0)
          it.putString(topicFilter)
          it.put(0b0000_0001 as byte)
        }
    when: 'invalid message flags'
        def inMessage = new SubscribeMqttInMessage(0b0000_0000 as byte)
        def successful = inMessage.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        !successful
        inMessage.exception() instanceof MalformedProtocolMqttException
        inMessage.exception().message == "Unexpected message flags:[0b0000_0000] in message:[$MqttMessageType.SUBSCRIBE]"
    when: 'invalid QoS or retain handling'
        def dataBuffer2 = BufferUtils.prepareBuffer(512) {
          it.putShort(testMessageId)
          it.putMbi(0)
          it.putString(topicFilter)
          it.put(0b0011_1001 as byte)
        }
        def inMessage2 = new SubscribeMqttInMessage(SubscribeMqttInMessage.MESSAGE_FLAGS)
        def successful2 = inMessage2.read(defaultMqtt5Connection, dataBuffer2, dataBuffer2.limit())
    then:
        !successful2
        inMessage2.exception() instanceof MalformedProtocolMqttException
        inMessage2.exception().message == MqttProtocolErrors.UNSUPPORTED_QOS_OR_RETAIN_HANDLING
    when: 'no any topic filter'
        def dataBuffer3 = BufferUtils.prepareBuffer(512) {
          it.putShort(testMessageId)
          it.putMbi(0)
        }
        def inMessage3 = new SubscribeMqttInMessage(SubscribeMqttInMessage.MESSAGE_FLAGS)
        def successful3 = inMessage3.read(defaultMqtt5Connection, dataBuffer3, dataBuffer3.limit())
    then:
        !successful3
        inMessage3.exception() instanceof MalformedProtocolMqttException
        inMessage3.exception().message == MqttProtocolErrors.NO_ANY_TOPIC_FILTERS
    when: 'invalid property'
        def propertiesBuffer = BufferUtils.prepareBuffer(512) {
          it.putProperty(MqttMessageProperty.SERVER_REFERENCE, "reference")
        }
        def dataBuffer4 = BufferUtils.prepareBuffer(512) {
          it.putShort(testMessageId)
          it.putMbi(propertiesBuffer.limit())
          it.put(propertiesBuffer)
        }
        def inMessage4 = new SubscribeMqttInMessage(SubscribeMqttInMessage.MESSAGE_FLAGS)
        def successful4 = inMessage4.read(defaultMqtt5Connection, dataBuffer4, dataBuffer4.limit())
    then:
        !successful4
        inMessage4.exception() instanceof MalformedProtocolMqttException
        inMessage4.exception().message == "Property:[$MqttMessageProperty.SERVER_REFERENCE] is not available for message:[$MqttMessageType.SUBSCRIBE]"
    when: 'invalid subscription id'
        def propertiesBuffer2 = BufferUtils.prepareBuffer(512) {
          it.putProperty(MqttMessageProperty.SUBSCRIPTION_IDENTIFIER, -50)
        }
        def dataBuffer5 = BufferUtils.prepareBuffer(512) {
          it.putShort(testMessageId)
          it.putMbi(propertiesBuffer2.limit())
          it.put(propertiesBuffer2)
        }
        def inMessage5 = new SubscribeMqttInMessage(SubscribeMqttInMessage.MESSAGE_FLAGS)
        def successful5 = inMessage5.read(defaultMqtt5Connection, dataBuffer5, dataBuffer5.limit())
    then:
        !successful5
        inMessage5.exception() instanceof MalformedProtocolMqttException
        inMessage5.exception().message == MqttProtocolErrors.INVALID_SUBSCRIPTION_ID
    when: 'two times provided subscription id'
        def propertiesBuffer3 = BufferUtils.prepareBuffer(512) {
          it.putProperty(MqttMessageProperty.SUBSCRIPTION_IDENTIFIER, 90)
          it.putProperty(MqttMessageProperty.SUBSCRIPTION_IDENTIFIER, 90)
        }
        def dataBuffer6 = BufferUtils.prepareBuffer(512) {
          it.putShort(testMessageId)
          it.putMbi(propertiesBuffer3.limit())
          it.put(propertiesBuffer3)
        }
        def inMessage6 = new SubscribeMqttInMessage(SubscribeMqttInMessage.MESSAGE_FLAGS)
        def successful6 = inMessage6.read(defaultMqtt5Connection, dataBuffer6, dataBuffer6.limit())
    then:
        !successful6
        inMessage6.exception() instanceof MalformedProtocolMqttException
        inMessage6.exception().message == "Property:[$MqttMessageProperty.SUBSCRIPTION_IDENTIFIER] is already presented in message:[$MqttMessageType.SUBSCRIBE]"
  }
}
