package javasabr.mqtt.service.publish.impl

import javasabr.mqtt.model.QoS
import javasabr.mqtt.model.data.type.StringPair
import javasabr.mqtt.model.publish.PublishData
import javasabr.mqtt.model.topic.TopicName
import javasabr.mqtt.test.support.UnitSpecification
import javasabr.rlib.collections.array.Array
import javasabr.rlib.collections.array.IntArray

import java.util.UUID

import static java.nio.charset.StandardCharsets.UTF_8

class InMemoryIncomingPublishStorageTest extends UnitSpecification {

  def "should store incoming publish with all attributes"() {
    given:
        def storage = new InMemoryIncomingPublishStorage()
        def publishId = UUID.randomUUID()
        def mainTopicName = TopicName.valueOf("topic/main")
        def replyTopicName = TopicName.valueOf("topic/response")
        def publishData = PublishData.wrap("payload".getBytes(UTF_8))
        def subscriberIds = IntArray.of(10, 20)
        def publishUserProperties = Array.of(
            new StringPair("key-1", "value-1"),
            new StringPair("key-2", "value-2"))
    when:
        def incomingPublish = storage.store(
            publishId,
            15,
            QoS.EXACTLY_ONCE,
            mainTopicName,
            replyTopicName,
            publishData,
            true,
            true,
            subscriberIds,
            60_000,
            7,
            publishUserProperties)
    then:
        storage.storedPublishes.size() == 1
        storage.storedPublishes.containsKey(publishId)
        with(incomingPublish) {
          id() == publishId
          messageId() == 15
          qos() == QoS.EXACTLY_ONCE
          topicName() == mainTopicName
          responseTopicName() == replyTopicName
          data() == publishData
          duplicated()
          retained()
          subscriptionIds() == subscriberIds
          messageExpiryInterval() == 60_000
          topicAlias() == 7
          userProperties() == publishUserProperties
        }
  }

  def "should not allow to store incoming publish twice with the same id"() {
    given:
        def storage = new InMemoryIncomingPublishStorage()
        def publishId = UUID.randomUUID()
        def topicName = TopicName.valueOf("topic/main")
        def publishData = PublishData.wrap("payload".getBytes(UTF_8))
        storage.store(
            publishId,
            1,
            QoS.AT_LEAST_ONCE,
            topicName,
            null,
            publishData,
            false,
            false,
            IntArray.empty(),
            60_000,
            0,
            Array.empty(StringPair))
    when:
        storage.store(
            publishId,
            2,
            QoS.AT_MOST_ONCE,
            topicName,
            null,
            publishData,
            false,
            false,
            IntArray.empty(),
            30_000,
            0,
            Array.empty(StringPair))
    then:
        def exception = thrown(IllegalArgumentException)
        exception.message == "Publish with id:[${publishId}] already exists"
        storage.storedPublishes.size() == 1
  }

  def "should remove stored incoming publish"() {
    given:
        def storage = new InMemoryIncomingPublishStorage()
        def incomingPublish = storePublish(storage)
    when:
        storage.remove(incomingPublish)
    then:
        storage.storedPublishes.isEmpty()
        !storage.storedPublishes.containsKey(incomingPublish.id())
  }

  def "should keep stored publish until all consumers are handled"() {
    given:
        def storage = new InMemoryIncomingPublishStorage()
        def incomingPublish = storePublish(storage)
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

  def "should ignore consumer count update for missing publish"() {
    given:
        def storage = new InMemoryIncomingPublishStorage()
        def incomingPublish = storePublish(storage)
        storage.remove(incomingPublish)
    when:
        storage.increaseConsumerCount(incomingPublish, 1)
    then:
        storage.storedPublishes.isEmpty()
  }

  def "should remove stored publish when consumer count is decreased below zero"() {
    given:
        def storage = new InMemoryIncomingPublishStorage()
        def incomingPublish = storePublish(storage)
        storage.increaseConsumerCount(incomingPublish, 1)
    when:
        storage.decreaseConsumerCount(incomingPublish, 2)
    then:
        storage.storedPublishes.isEmpty()
  }

  private static def storePublish(InMemoryIncomingPublishStorage storage) {
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
