package javasabr.mqtt.model.topic.tree

import javasabr.mqtt.model.publishing.Publish
import javasabr.mqtt.model.topic.TopicFilter
import javasabr.mqtt.test.support.UnitSpecification

import static javasabr.mqtt.model.subscription.TestPublishFactory.makePublish

class RetainedMessageTreeTest extends UnitSpecification {

  def "should fetch retained messages by topic filter"(
      List<Publish> messages,
      String topicFilter,
      List<Publish> expectedMessages) {
    given:
        ConcurrentRetainedMessageTree retainedMessageTree = new ConcurrentRetainedMessageTree();
        messages.eachWithIndex { Publish message, int i ->
          retainedMessageTree.retainMessage(message)
        }
    when:
        def retainedMessages = retainedMessageTree.getRetainedMessage(TopicFilter.valueOf(topicFilter))
            .collect { it }
    then:
        retainedMessages.size() == expectedMessages.size()
        for (int i = 0; i < retainedMessages.size(); i++) {
          assert retainedMessages.get(i).topicName() == expectedMessages.get(i).topicName()
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
                makePublish("/topic/segment1"),
                makePublish("/topic/segment2"),
                makePublish("/topic/segment1/segment2"),
                makePublish("/topic/"),
                makePublish("/topic")
            ],
            [
                makePublish("/topic/segment1"),
                makePublish("/topic/segment2"),
                makePublish("/topic/segment1/segment2"),
                makePublish("/topic/"),
                makePublish("/topic/segment2"),
                makePublish("/"),
                makePublish("/topic/segment2/segment1")
            ],
            [
                makePublish("/topic/segment1"),
                makePublish("/topic/segment2"),
                makePublish("/topic/segment3"),
                makePublish("/topic/segment3"),
                makePublish("/topic/segment3"),
                makePublish("/topic/segment3")
            ],
            [
                makePublish("/topic/segment1"),
                makePublish("/topic/segment2"),
                makePublish("/topic/segment1/segment2"),
                makePublish("/topic/segment500/segment2"),
                makePublish("/topic/"),
                makePublish("/topic")
            ],
            [
                makePublish("/topic1/segment1"),
                makePublish("/topic/segment2"),
                makePublish("/topic2/segment1/segment2"),
                makePublish("/topic/segment3"),
                makePublish("/topic/segment1/segment2")
            ]
        ]
        expectedMessages << [
            [
                makePublish("/topic/segment1")
            ],
            [
                makePublish("/topic/segment2")
            ],
            [
                makePublish("/topic/segment3")
            ],
            [
                makePublish("/topic/segment1/segment2"),
                makePublish("/topic/segment500/segment2")
            ],
            [
                makePublish("/topic/segment2"),
                makePublish("/topic/segment3"),
                makePublish("/topic/segment1/segment2")
            ]
        ]
  }
}
