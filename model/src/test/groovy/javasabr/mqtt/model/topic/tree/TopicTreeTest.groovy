package javasabr.mqtt.model.topic.tree

import javasabr.mqtt.model.QoS
import javasabr.mqtt.model.SubscribeRetainHandling
import javasabr.mqtt.model.subscriber.SingleSubscriber
import javasabr.mqtt.model.subscribtion.Subscription
import javasabr.mqtt.model.subscribtion.SubscriptionOwner
import javasabr.mqtt.model.subscription.TestSubscriptionOwner
import javasabr.mqtt.model.topic.SharedTopicFilter
import javasabr.mqtt.model.topic.TopicFilter
import javasabr.mqtt.model.topic.TopicName
import javasabr.mqtt.test.support.UnitSpecification

class TopicTreeTest extends UnitSpecification {

  def "should match simple topic correctly"(
      List<Subscription> subscriptions,
      List<SubscriptionOwner> owners,
      String topicName,
      List<SubscriptionOwner> expectedOwners) {
    given:
        ConcurrentTopicTree topicTree = new ConcurrentTopicTree()
        subscriptions.eachWithIndex { Subscription subscription, int i ->
          topicTree.subscribe(owners.get(i), subscription)
        }
    when:
        def found = topicTree.matches(TopicName.valueOf(topicName))
            .collect { it.resolveOwner() }
    then:
        found ==~ expectedOwners
    where:
        topicName << [
            "/topic/segment1",
            "/topic/segment2",
            "/topic/segment3"
        ]
        subscriptions << [
            [
                makeSubscription("/topic/segment1"),
                makeSubscription("/topic/segment2"),
                makeSubscription("/topic/segment1/segment2"),
                makeSubscription("/topic/"),
                makeSubscription("/topic")
            ],
            [
                makeSubscription("/topic/segment1"),
                makeSubscription("/topic/segment2"),
                makeSubscription("/topic/segment1/segment2"),
                makeSubscription("/topic/"),
                makeSubscription("/topic/segment2"),
                makeSubscription("/"),
                makeSubscription("/topic/segment2/segment1")
            ],
            [
                makeSubscription("/topic/segment1"),
                makeSubscription("/topic/segment2"),
                makeSubscription("/topic/segment3"),
                makeSubscription("/topic/segment3"),
                makeSubscription("/topic/segment3"),
                makeSubscription("/topic/segment3")
            ]
        ]
        owners << [
            [
                makeOwner("id1"),
                makeOwner("id2"),
                makeOwner("id3"),
                makeOwner("id4"),
                makeOwner("id5")
            ],
            [
                makeOwner("id1"),
                makeOwner("id2"),
                makeOwner("id3"),
                makeOwner("id4"),
                makeOwner("id5"),
                makeOwner("id6"),
                makeOwner("id7")
            ],
            [
                makeOwner("id1"),
                makeOwner("id2"),
                makeOwner("id3"),
                makeOwner("id3"),
                makeOwner("id3"),
                makeOwner("id4")
            ]
        ]
        expectedOwners << [
            [
                makeOwner("id1")
            ],
            [
                makeOwner("id2"),
                makeOwner("id5")
            ],
            [
                makeOwner("id3"),
                makeOwner("id4")
            ]
        ]
  }

  def "should match single wildcard topic correctly"(
      List<Subscription> subscriptions,
      List<SubscriptionOwner> owners,
      String topicName,
      List<SubscriptionOwner> expectedOwners) {
    given:
        ConcurrentTopicTree topicTree = new ConcurrentTopicTree()
        subscriptions.eachWithIndex { Subscription subscription, int i ->
          topicTree.subscribe(owners.get(i), subscription)
        }
    when:
        def found = topicTree.matches(TopicName.valueOf(topicName))
            .collect { it.resolveOwner() }
    then:
        found ==~ expectedOwners
    where:
        topicName << [
            "/topic/segment1",
            "/topic/segment2",
            "/topic/segment3"
        ]
        subscriptions << [
            [
                makeSubscription("/topic/segment1"),
                makeSubscription("/topic/+"),
                makeSubscription("/+/segment1"),
                makeSubscription("/+/+"),
                makeSubscription("/topic/segment2"),
                makeSubscription("/topic2/segment1"),
                makeSubscription("/+/segment2"),
                makeSubscription("/topic2/+")
            ],
            [
                makeSubscription("/topic/segment1"),
                makeSubscription("/topic/+"),
                makeSubscription("/+/segment1"),
                makeSubscription("/+/+"),
                makeSubscription("/topic/segment2"),
                makeSubscription("/topic2/segment1"),
                makeSubscription("/+/segment2"),
                makeSubscription("/topic2/+")
            ],
            [
                makeSubscription("/topic/segment1"),
                makeSubscription("/topic/+"),
                makeSubscription("/+/segment1"),
                makeSubscription("/+/+"),
                makeSubscription("/topic/segment2"),
                makeSubscription("/topic2/segment1"),
                makeSubscription("/+/segment2"),
                makeSubscription("/topic2/+")
            ]
        ]
        owners << [
            [
                makeOwner("id1"),
                makeOwner("id2"),
                makeOwner("id3"),
                makeOwner("id4"),
                makeOwner("id5"),
                makeOwner("id6"),
                makeOwner("id7"),
                makeOwner("id8")
            ],
            [
                makeOwner("id1"),
                makeOwner("id2"),
                makeOwner("id3"),
                makeOwner("id4"),
                makeOwner("id5"),
                makeOwner("id6"),
                makeOwner("id7"),
                makeOwner("id8")
            ],
            [
                makeOwner("id1"),
                makeOwner("id2"),
                makeOwner("id3"),
                makeOwner("id4"),
                makeOwner("id5"),
                makeOwner("id6"),
                makeOwner("id7"),
                makeOwner("id8")
            ]
        ]
        expectedOwners << [
            [
                makeOwner("id1"),
                makeOwner("id2"),
                makeOwner("id3"),
                makeOwner("id4")
            ],
            [
                makeOwner("id2"),
                makeOwner("id4"),
                makeOwner("id5"),
                makeOwner("id7")
            ],
            [
                makeOwner("id2"),
                makeOwner("id4")
            ]
        ]
  }

  def "should match multi wildcard topic correctly"(
      List<Subscription> subscriptions,
      List<SubscriptionOwner> owners,
      String topicName,
      List<SubscriptionOwner> expectedOwners) {
    given:
        ConcurrentTopicTree topicTree = new ConcurrentTopicTree()
        subscriptions.eachWithIndex { Subscription subscription, int i ->
          topicTree.subscribe(owners.get(i), subscription)
        }
    when:
        def found = topicTree.matches(TopicName.valueOf(topicName))
            .collect { it.resolveOwner() }
    then:
        found ==~ expectedOwners
    where:
        topicName << [
            "/topic/segment1/segment2",
            "/topic/segment3/segment4",
            "/topic/segment2"
        ]
        subscriptions << [
            [
                makeSubscription("/topic/segment1/segment2"),
                makeSubscription("/topic/segment1/#"),
                makeSubscription("/topic/#"),
                makeSubscription("/#"),
                makeSubscription("#"),
                makeSubscription("/topic/segment2/segment3"),
                makeSubscription("/topic/segment2/#"),
                makeSubscription("/topic/segment3/segment4"),
                makeSubscription("/topic/segment3/#")
            ],
            [
                makeSubscription("/topic/segment1/segment2"),
                makeSubscription("/topic/segment1/#"),
                makeSubscription("/topic/#"),
                makeSubscription("/#"),
                makeSubscription("#"),
                makeSubscription("/topic/segment2/segment3"),
                makeSubscription("/topic/segment2/#"),
                makeSubscription("/topic/segment3/segment4"),
                makeSubscription("/topic/segment3/#")
            ],
            [
                makeSubscription("/topic/segment1/segment2"),
                makeSubscription("/topic/segment1/#"),
                makeSubscription("/topic/#"),
                makeSubscription("/#"),
                makeSubscription("#"),
                makeSubscription("/topic/segment2/segment3"),
                makeSubscription("/topic/segment2/#"),
                makeSubscription("/topic/segment3/segment4"),
                makeSubscription("/topic/segment3/#")
            ]
        ]
        owners << [
            [
                makeOwner("id1"),
                makeOwner("id2"),
                makeOwner("id3"),
                makeOwner("id4"),
                makeOwner("id5"),
                makeOwner("id6"),
                makeOwner("id7"),
                makeOwner("id8"),
                makeOwner("id9")
            ],
            [
                makeOwner("id1"),
                makeOwner("id2"),
                makeOwner("id3"),
                makeOwner("id4"),
                makeOwner("id5"),
                makeOwner("id6"),
                makeOwner("id7"),
                makeOwner("id8"),
                makeOwner("id9")
            ],
            [
                makeOwner("id1"),
                makeOwner("id2"),
                makeOwner("id3"),
                makeOwner("id4"),
                makeOwner("id5"),
                makeOwner("id6"),
                makeOwner("id7"),
                makeOwner("id8"),
                makeOwner("id9")
            ]
        ]
        expectedOwners << [
            [
                makeOwner("id1"),
                makeOwner("id2"),
                makeOwner("id3"),
                makeOwner("id4"),
                makeOwner("id5")
            ],
            [
                makeOwner("id8"),
                makeOwner("id9"),
                makeOwner("id3"),
                makeOwner("id4"),
                makeOwner("id5")
            ],
            [
                makeOwner("id3"),
                makeOwner("id4"),
                makeOwner("id5")
            ]
        ]
  }

  def "should choose strongest QoS when the same subscriber has several matches"(
      List<Subscription> subscriptions,
      List<SubscriptionOwner> owners,
      String topicName,
      List<SingleSubscriber> expectedSubscribers) {
    given:
        ConcurrentTopicTree topicTree = new ConcurrentTopicTree()
        subscriptions.eachWithIndex { Subscription subscription, int i ->
          topicTree.subscribe(owners.get(i), subscription)
        }
    when:
        def found = topicTree.matches(TopicName.valueOf(topicName))
    then:
        found ==~ expectedSubscribers
    where:
        topicName << [
            "/topic/segment1/segment2",
            "/topic/segment3",
            "/topic/segment2/"
        ]
        subscriptions << [
            [
                makeSubscription("/topic/segment1/segment2", 2),
                makeSubscription("/topic/segment1/#", 1),
                makeSubscription("/topic/#", 0),
                makeSubscription("/topic/segment1/segment3", 2),
                makeSubscription("/topic/segment1/#", 1),
                makeSubscription("/topic/#", 0),
                makeSubscription("/topic/segment2/segment3", 2),
                makeSubscription("/topic/segment2/#", 1),
                makeSubscription("/topic/#", 0)
            ],
            [
                makeSubscription("/topic/segment1/segment2", 2),
                makeSubscription("/topic/segment1/#", 1),
                makeSubscription("/topic/#", 0),
                makeSubscription("/topic/segment1/segment3", 2),
                makeSubscription("/topic/segment1/#", 1),
                makeSubscription("/topic/#", 0),
                makeSubscription("/topic/segment2/segment3", 2),
                makeSubscription("/topic/segment2/#", 1),
                makeSubscription("/topic/#", 0)
            ],
            [
                makeSubscription("/topic/segment1/segment2", 2),
                makeSubscription("/topic/segment1/#", 1),
                makeSubscription("/topic/#", 0),
                makeSubscription("/topic/segment1/segment3", 2),
                makeSubscription("/topic/segment1/#", 1),
                makeSubscription("/topic/#", 0),
                makeSubscription("/topic/segment2/segment3", 2),
                makeSubscription("/topic/segment2/#", 1),
                makeSubscription("/topic/#", 0)
            ]
        ]
        owners << [
            [
                makeOwner("id1"),
                makeOwner("id1"),
                makeOwner("id1"),
                makeOwner("id2"),
                makeOwner("id2"),
                makeOwner("id2"),
                makeOwner("id3"),
                makeOwner("id3"),
                makeOwner("id3")
            ],
            [
                makeOwner("id1"),
                makeOwner("id1"),
                makeOwner("id1"),
                makeOwner("id2"),
                makeOwner("id2"),
                makeOwner("id2"),
                makeOwner("id3"),
                makeOwner("id3"),
                makeOwner("id3")
            ],
            [
                makeOwner("id1"),
                makeOwner("id1"),
                makeOwner("id1"),
                makeOwner("id2"),
                makeOwner("id2"),
                makeOwner("id2"),
                makeOwner("id3"),
                makeOwner("id3"),
                makeOwner("id3")
            ]
        ]
        expectedSubscribers << [
            [
                new SingleSubscriber(makeOwner("id1"), makeSubscription("/topic/segment1/segment2", 2)),
                new SingleSubscriber(makeOwner("id2"), makeSubscription("/topic/segment1/#", 1)),
                new SingleSubscriber(makeOwner("id3"), makeSubscription("/topic/#", 0)),
            ],
            [
                new SingleSubscriber(makeOwner("id1"), makeSubscription("/topic/#", 0)),
                new SingleSubscriber(makeOwner("id2"), makeSubscription("/topic/#", 0)),
                new SingleSubscriber(makeOwner("id3"), makeSubscription("/topic/#", 0)),
            ],
            [
                new SingleSubscriber(makeOwner("id1"), makeSubscription("/topic/#", 0)),
                new SingleSubscriber(makeOwner("id2"), makeSubscription("/topic/#", 0)),
                new SingleSubscriber(makeOwner("id3"), makeSubscription("/topic/segment2/#", 1)),
            ]
        ]
  }

  def "should provide different owners when math shared topic"() {
    given:
        def group1 = ["id1", "id2", "id3", "id4", "id5"]
        def group2 = ["id6", "id7", "id8", "id9", "id10"]
        ConcurrentTopicTree topicTree = new ConcurrentTopicTree()
        topicTree.subscribe(makeOwner("id1"), makeSharedSubscription('$share/group1/topic/name1'))
        topicTree.subscribe(makeOwner("id2"), makeSharedSubscription('$share/group1/topic/name1'))
        topicTree.subscribe(makeOwner("id3"), makeSharedSubscription('$share/group1/topic/name1'))
        topicTree.subscribe(makeOwner("id4"), makeSharedSubscription('$share/group1/topic/name1'))
        topicTree.subscribe(makeOwner("id5"), makeSharedSubscription('$share/group1/topic/name1'))
        topicTree.subscribe(makeOwner("id6"), makeSharedSubscription('$share/group2/topic/name1'))
        topicTree.subscribe(makeOwner("id7"), makeSharedSubscription('$share/group2/topic/name1'))
        topicTree.subscribe(makeOwner("id8"), makeSharedSubscription('$share/group2/topic/name1'))
        topicTree.subscribe(makeOwner("id9"), makeSharedSubscription('$share/group2/topic/name1'))
        topicTree.subscribe(makeOwner("id10"), makeSharedSubscription('$share/group2/topic/name1'))
    when:
        def matched = topicTree
            .matches(TopicName.valueOf("topic/name1"))
            .collect { it.owner().toString() }
    then:
        matched.size() == 2
    when:
        def matched2 = topicTree
            .matches(TopicName.valueOf("topic/name1"))
            .collect { it.owner().toString() }
    then:
        matched2.size() == 2
        matched2 != matched
    then: "should contains by one owner from different groups"
        (group1.contains(matched[0]) && group2.contains(matched[1])) ||
            (group1.contains(matched[1]) && group2.contains(matched[0]))
        (group1.contains(matched2[0]) && group2.contains(matched2[1])) ||
            (group1.contains(matched2[1]) && group2.contains(matched2[0]))

  }

  static def makeOwner(String id) {
    return new TestSubscriptionOwner(id)
  }

  static def makeSubscription(String topicFilter) {
    return new Subscription(
        TopicFilter.valueOf(topicFilter),
        QoS.AT_LEAST_ONCE,
        SubscribeRetainHandling.SEND,
        true,
        true)
  }

  static def makeSharedSubscription(String topicFilter) {
    return new Subscription(
        SharedTopicFilter.valueOf(topicFilter),
        QoS.AT_LEAST_ONCE,
        SubscribeRetainHandling.SEND,
        true,
        true)
  }

  static def makeSubscription(String topicFilter, int qos) {
    return new Subscription(
        TopicFilter.valueOf(topicFilter),
        QoS.ofCode(qos),
        SubscribeRetainHandling.SEND,
        true,
        true)
  }
}
