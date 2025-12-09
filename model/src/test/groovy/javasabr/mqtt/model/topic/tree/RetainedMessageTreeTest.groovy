package javasabr.mqtt.model.topic.tree

import javasabr.mqtt.model.publishing.Publish
import javasabr.mqtt.model.topic.TopicFilter
import javasabr.mqtt.test.support.UnitSpecification

import static javasabr.mqtt.model.subscription.TestPublishFactory.createPublish

class RetainedMessageTreeTest extends UnitSpecification {

  def "should fetch retained messages by topic filter"(
      List<String> messages,
      String topicFilter,
      List<String> expectedMessages) {
    given:
        ConcurrentRetainedMessageTree retainedMessageTree = new ConcurrentRetainedMessageTree();
        messages.collect { createPublish(it) }.eachWithIndex { Publish message, int i ->
          retainedMessageTree.retainMessage(message)
        }
    when:
        def retainedMessages = retainedMessageTree.getRetainedMessage(TopicFilter.valueOf(topicFilter))
            .collect { it }
    then:
        retainedMessages.size() == expectedMessages.size()
        for (int i = 0; i < retainedMessages.size(); i++) {
          assert retainedMessages[i].topicName().rawTopic() == expectedMessages[i]
        }
    where:
        topicFilter << [
            "/topic/segment1",
            "/topic/segment2",
            "/topic/segment3",
            "/topic/+/segment2",
            "/topic/#"
        ]
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
                "/topic/segment2",
                "/topic/segment3",
                "/topic/segment1/segment2"
            ]
        ]
  }
}
