package javasabr.mqtt.model.topic.tree


import javasabr.mqtt.model.subscription.TestPublishFactory
import javasabr.mqtt.model.topic.TopicFilter
import javasabr.mqtt.model.topic.TopicName
import javasabr.mqtt.test.support.UnitSpecification

class RetainedMessageTreeTest extends UnitSpecification {

  def "should fetch retained messages by topic filter"(
      List<String> messages,
      String rawTopicFilter,
      List<String> expectedMessages) {
    given:
        ConcurrentRetainedMessageTree retainedMessageTree = new ConcurrentRetainedMessageTree()
        messages
            .collect(TestPublishFactory::incomingQos0Publish)
            .each(retainedMessageTree::addRetainedMessage)
        def topicFilter = TopicFilter.valueOf(rawTopicFilter)
    when:
        def retainedMessages = retainedMessageTree.getRetainedMessages(topicFilter)
    then:
        retainedMessages.size() == expectedMessages.size()
        verifyEach(retainedMessages) { publish, index ->
          publish.topicName().rawTopic() == expectedMessages[index]
        }
    where:
        rawTopicFilter << [
            "/topic/segment1",
            "/topic/segment2",
            "/topic/segment3",
            "/topic/+/segment2",
            "/topic/#"
        ]
        //noinspection GroovyAssignabilityCheck
        messages << [
            [
                "/topic/segment1",
                "/topic/segment2",
                "/topic/segment1/segment2",
                "/topic/",
                "/topic"
            ],
            [
                "/topic/segment1",
                "/topic/segment2",
                "/topic/segment1/segment2",
                "/topic/",
                "/topic/segment2",
                "/",
                "/topic/segment2/segment1"
            ],
            [
                "/topic/segment1",
                "/topic/segment2",
                "/topic/segment3",
                "/topic/segment3",
                "/topic/segment3",
                "/topic/segment3"
            ],
            [
                "/topic/segment1",
                "/topic/segment2",
                "/topic/segment1/segment2",
                "/topic/segment500/segment2",
                "/topic/",
                "/topic"
            ],
            [
                "/topic1/segment1",
                "/topic/segment2",
                "/topic2/segment1/segment2",
                "/topic/segment3",
                "/topic/segment1/segment2"
            ]
        ]
        //noinspection GroovyAssignabilityCheck
        expectedMessages << [
            [
                "/topic/segment1"
            ],
            [
                "/topic/segment2"
            ],
            [
                "/topic/segment3"
            ],
            [
                "/topic/segment1/segment2",
                "/topic/segment500/segment2"
            ],
            [
                "/topic/segment1/segment2",
                "/topic/segment2",
                "/topic/segment3"
            ]
        ]
  }

  def "should add and remove retained messages to and from retained message tree"() {
    given:
        def publish = TestPublishFactory.incomingQos0Publish("topic")
        ConcurrentRetainedMessageTree retainedMessageTree = new ConcurrentRetainedMessageTree()
    when:
        retainedMessageTree.addRetainedMessage(publish)
    then:
        with(retainedMessageTree.getRetainedMessages(TopicFilter.valueOf("topic"))) {
          size() == 1
          first() == publish
        }
    when:
        retainedMessageTree.removeRetainedMessage(TopicName.valueOf("topic"))
    then:
        with(retainedMessageTree.getRetainedMessages(TopicFilter.valueOf("topic"))) {
          isEmpty()
        }
  }
}
