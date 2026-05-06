package javasabr.mqtt.service.publish.impl

import javasabr.mqtt.model.MqttProperties
import javasabr.mqtt.model.QoS
import javasabr.mqtt.model.data.type.StringPair
import javasabr.mqtt.model.publish.PublishData
import javasabr.mqtt.model.topic.TopicName
import javasabr.mqtt.network.message.in.MqttInMessage
import javasabr.mqtt.test.support.UnitSpecification
import javasabr.rlib.collections.array.Array
import javasabr.rlib.collections.array.IntArray

import static java.nio.charset.StandardCharsets.UTF_8

class InMemoryIncomingPublishStorageTest extends UnitSpecification {

  def "should store incoming publish with all attributes"() {
    given:
        def storage = new InMemoryIncomingPublishStorage()
        def testPublishId = UUID.randomUUID()
        def testTopicAlias = 7
        def testMessageId = 15
        def testQos = QoS.EXACTLY_ONCE
        def testTopicName = TopicName.valueOf("topic/main")
        def testResponseTopicName = TopicName.valueOf("topic/response")
        def testData = PublishData.wrap("payload".getBytes(UTF_8))
        def testSubscriptionIds = IntArray.of(10, 20)
        def testUserProperties = Array.of(
            new StringPair("key-1", "value-1"),
            new StringPair("key-2", "value-2"))
    when:
        def incomingPublish = storage.store(
            testPublishId,
            testMessageId,
            testQos,
            testTopicName,
            testResponseTopicName,
            testData,
            true,
            true,
            testSubscriptionIds,
            60_000,
            testTopicAlias,
            testUserProperties)
    then:
        storage.storedPublishes.size() == 1
        storage.storedPublishes.containsKey(testPublishId)
        with(incomingPublish) {
          id() == testPublishId
          messageId() == testMessageId
          qos() == testQos
          topicName() == testTopicName
          responseTopicName() == testResponseTopicName
          data() == testData
          duplicated()
          retained()
          subscriptionIds() == testSubscriptionIds
          messageExpiryInterval() == 60_000
          topicAlias() == testTopicAlias
          userProperties() == testUserProperties
        }
  }

  def "should not allow to store incoming publish twice with the same id"() {
    given:
        def storage = new InMemoryIncomingPublishStorage()
        def publishId = UUID.randomUUID()
        def topicName = TopicName.valueOf("topic/main")
        def data = PublishData.wrap("payload".getBytes(UTF_8))
        def testMessageId1 = 6
        def testMessageId2 = 9
        storage.store(
            publishId,
            testMessageId1,
            QoS.AT_LEAST_ONCE,
            topicName,
            null,
            data,
            false,
            false,
            IntArray.empty(),
            60_000,
            MqttProperties.TOPIC_ALIAS_MAX_IS_NOT_SET,
            MqttInMessage.EMPTY_USER_PROPERTIES)

    when:
        storage.store(
            publishId,
            testMessageId2,
            QoS.AT_MOST_ONCE,
            topicName,
            null,
            data,
            false,
            false,
            IntArray.empty(),
            30_000,
            MqttProperties.TOPIC_ALIAS_MAX_IS_NOT_SET,
            MqttInMessage.EMPTY_USER_PROPERTIES)
    then:
        def exception = thrown(IllegalArgumentException)
        exception.message == "Publish with id:[${publishId}] already exists"
        storage.storedPublishes.size() == 1
  }

  def "should remove stored incoming publish"() {
    given:
        def storage = new InMemoryIncomingPublishStorage()
        def incomingPublish = createAndStorePublish(storage)
    when:
        storage.remove(incomingPublish)
    then:
        storage.storedPublishes.isEmpty()
        !storage.storedPublishes.containsKey(incomingPublish.id())
  }

  def "should keep stored publish until all consumers are handled"() {
    given:
        def storage = new InMemoryIncomingPublishStorage()
        def incomingPublish = createAndStorePublish(storage)
        storage.increaseConsumerCount(incomingPublish, 3)
    when:
        storage.decreaseConsumerCount(incomingPublish, 2)
    then:
        storage.storedPublishes.size() == 1
        storage.storedPublishes.containsKey(incomingPublish.id())
    when:
        storage.decreaseConsumerCount(incomingPublish, 1)
    then:
        storage.storedPublishes.isEmpty()
  }

  def "should not allow to change consumer count update for missing publish"() {
    given:
        def storage = new InMemoryIncomingPublishStorage()
        def incomingPublish = createAndStorePublish(storage)
        storage.remove(incomingPublish)
    when:
        storage.increaseConsumerCount(incomingPublish, 1)
    then:
        def exception = thrown(IllegalArgumentException)
        exception.message == "Unknown publish:[${incomingPublish.id()}]"
        storage.storedPublishes.isEmpty()
  }

  def "should remove stored publish when consumer count is decreased below zero"() {
    given:
        def storage = new InMemoryIncomingPublishStorage()
        def incomingPublish = createAndStorePublish(storage)
        storage.increaseConsumerCount(incomingPublish, 1)
    when:
        storage.decreaseConsumerCount(incomingPublish, 2)
    then:
        storage.storedPublishes.isEmpty()
  }

  private static def createAndStorePublish(InMemoryIncomingPublishStorage storage) {
    return storage.store(
        UUID.randomUUID(),
        1,
        QoS.AT_LEAST_ONCE,
        TopicName.valueOf("topic/main"),
        null,
        PublishData.wrap("payload".getBytes(UTF_8)),
        false,
        false,
        IntArray.of(5),
        60_000,
        3,
        Array.of(new StringPair("key", "value")))
  }
}
