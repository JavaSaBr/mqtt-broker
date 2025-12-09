package javasabr.mqtt.model.topic.tree

import javasabr.mqtt.model.subscription.TestPublishFactory
import javasabr.mqtt.model.topic.TopicFilter
import javasabr.mqtt.test.support.UnitSpecification

class RetainedMessageTreeTest extends UnitSpecification {

  def "should fetch retained messages by topic filter"(
      List<String> messages,
      String rawTopicFilter,
      List<String> expectedMessages) {
    given:
        ConcurrentRetainedMessageTree retainedMessageTree = new ConcurrentRetainedMessageTree();
        messages.collect(TestPublishFactory::createPublish).each(retainedMessageTree::retainMessage)
        def topicFilter = TopicFilter.valueOf(rawTopicFilter)
    when:
        def retainedMessages = retainedMessageTree.getRetainedMessage(topicFilter)
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
