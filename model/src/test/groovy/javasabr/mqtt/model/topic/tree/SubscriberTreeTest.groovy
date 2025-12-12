package javasabr.mqtt.model.topic.tree

import javasabr.mqtt.model.MqttProperties
import javasabr.mqtt.model.MqttUser
import javasabr.mqtt.model.QoS
import javasabr.mqtt.model.SubscribeRetainHandling
import javasabr.mqtt.model.subscriber.SingleSubscriber
import javasabr.mqtt.model.subscriber.tree.ConcurrentSubscriberTree
import javasabr.mqtt.model.subscription.Subscription
import javasabr.mqtt.model.subscription.TestMqttUser
import javasabr.mqtt.model.topic.SharedTopicFilter
import javasabr.mqtt.model.topic.TopicFilter
import javasabr.mqtt.model.topic.TopicName
import javasabr.mqtt.test.support.UnitSpecification

class SubscriberTreeTest extends UnitSpecification {

  static SingleSubscriber createSubscriber(String clientId, String rawTopicFilter) {
    return createSubscriber(clientId, rawTopicFilter, QoS.AT_LEAST_ONCE.number())
  }

  static SingleSubscriber createSubscriber(String clientId, String rawTopicFilter, int qos) {
    return new SingleSubscriber(makeUser(clientId), makeSubscription(rawTopicFilter, qos))
  }

  static SingleSubscriber createShareSubscriber(String clientId, String rawTopicFilter) {
    return new SingleSubscriber(makeUser(clientId), makeSharedSubscription(rawTopicFilter))
  }

  def "should match simple topic correctly"(
      List<Subscription> subscriptions,
      List<MqttUser> users,
      String topicName,
      List<MqttUser> expectedUsers) {
    given:
        ConcurrentSubscriberTree subscriberTree = new ConcurrentSubscriberTree()
        subscriptions.eachWithIndex { Subscription subscription, int i ->
          subscriberTree.subscribe(new SingleSubscriber(users.get(i), subscription))
        }
    when:
        def found = subscriberTree.matches(TopicName.valueOf(topicName))
            .collect { it.resolveUser() }
    then:
        found ==~ expectedUsers
    where:
        topicName << [
            "/topic/segment1",
            "/topic/segment2",
            "/topic/segment3"
        ]
        //noinspection GroovyAssignabilityCheck
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
        //noinspection GroovyAssignabilityCheck
        users << [
            [
                makeUser("id1"),
                makeUser("id2"),
                makeUser("id3"),
                makeUser("id4"),
                makeUser("id5")
            ],
            [
                makeUser("id1"),
                makeUser("id2"),
                makeUser("id3"),
                makeUser("id4"),
                makeUser("id5"),
                makeUser("id6"),
                makeUser("id7")
            ],
            [
                makeUser("id1"),
                makeUser("id2"),
                makeUser("id3"),
                makeUser("id3"),
                makeUser("id3"),
                makeUser("id4")
            ]
        ]
        //noinspection GroovyAssignabilityCheck
        expectedUsers << [
            [
                makeUser("id1")
            ],
            [
                makeUser("id2"),
                makeUser("id5")
            ],
            [
                makeUser("id3"),
                makeUser("id4")
            ]
        ]
  }

  def "should match single wildcard topic correctly"(
      List<Subscription> subscriptions,
      List<MqttUser> users,
      String topicName,
      List<MqttUser> expectedUsers) {
    given:
        ConcurrentSubscriberTree subscriberTree = new ConcurrentSubscriberTree()
        subscriptions.eachWithIndex { Subscription subscription, int i ->
          subscriberTree.subscribe(new SingleSubscriber(users.get(i), subscription))
        }
    when:
        def found = subscriberTree.matches(TopicName.valueOf(topicName))
            .collect { it.resolveUser() }
    then:
        found ==~ expectedUsers
    where:
        topicName << [
            "/topic/segment1",
            "/topic/segment2",
            "/topic/segment3"
        ]
        //noinspection GroovyAssignabilityCheck
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
        //noinspection GroovyAssignabilityCheck
        users << [
            [
                makeUser("id1"),
                makeUser("id2"),
                makeUser("id3"),
                makeUser("id4"),
                makeUser("id5"),
                makeUser("id6"),
                makeUser("id7"),
                makeUser("id8")
            ],
            [
                makeUser("id1"),
                makeUser("id2"),
                makeUser("id3"),
                makeUser("id4"),
                makeUser("id5"),
                makeUser("id6"),
                makeUser("id7"),
                makeUser("id8")
            ],
            [
                makeUser("id1"),
                makeUser("id2"),
                makeUser("id3"),
                makeUser("id4"),
                makeUser("id5"),
                makeUser("id6"),
                makeUser("id7"),
                makeUser("id8")
            ]
        ]
        //noinspection GroovyAssignabilityCheck
        expectedUsers << [
            [
                makeUser("id1"),
                makeUser("id2"),
                makeUser("id3"),
                makeUser("id4")
            ],
            [
                makeUser("id2"),
                makeUser("id4"),
                makeUser("id5"),
                makeUser("id7")
            ],
            [
                makeUser("id2"),
                makeUser("id4")
            ]
        ]
  }

  def "should match multi wildcard topic correctly"(
      List<Subscription> subscriptions,
      List<MqttUser> users,
      String topicName,
      List<MqttUser> expectedUsers) {
    given:
        ConcurrentSubscriberTree subscriberTree = new ConcurrentSubscriberTree()
        subscriptions.eachWithIndex { Subscription subscription, int i ->
          subscriberTree.subscribe(new SingleSubscriber(users.get(i), subscription))
        }
    when:
        def found = subscriberTree.matches(TopicName.valueOf(topicName))
            .collect { it.resolveUser() }
    then:
        found ==~ expectedUsers
    where:
        topicName << [
            "/topic/segment1/segment2",
            "/topic/segment3/segment4",
            "/topic/segment2"
        ]
        //noinspection GroovyAssignabilityCheck
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
        //noinspection GroovyAssignabilityCheck
        users << [
            [
                makeUser("id1"),
                makeUser("id2"),
                makeUser("id3"),
                makeUser("id4"),
                makeUser("id5"),
                makeUser("id6"),
                makeUser("id7"),
                makeUser("id8"),
                makeUser("id9")
            ],
            [
                makeUser("id1"),
                makeUser("id2"),
                makeUser("id3"),
                makeUser("id4"),
                makeUser("id5"),
                makeUser("id6"),
                makeUser("id7"),
                makeUser("id8"),
                makeUser("id9")
            ],
            [
                makeUser("id1"),
                makeUser("id2"),
                makeUser("id3"),
                makeUser("id4"),
                makeUser("id5"),
                makeUser("id6"),
                makeUser("id7"),
                makeUser("id8"),
                makeUser("id9")
            ]
        ]
        //noinspection GroovyAssignabilityCheck
        expectedUsers << [
            [
                makeUser("id1"),
                makeUser("id2"),
                makeUser("id3"),
                makeUser("id4"),
                makeUser("id5")
            ],
            [
                makeUser("id8"),
                makeUser("id9"),
                makeUser("id3"),
                makeUser("id4"),
                makeUser("id5")
            ],
            [
                makeUser("id3"),
                makeUser("id4"),
                makeUser("id5")
            ]
        ]
  }

  def "should choose strongest QoS when the same subscriber has several matches"(
      List<Subscription> subscriptions,
      List<MqttUser> users,
      String topicName,
      List<SingleSubscriber> expectedSubscribers) {
    given:
        ConcurrentSubscriberTree subscriberTree = new ConcurrentSubscriberTree()
        subscriptions.eachWithIndex { Subscription subscription, int i ->
          subscriberTree.subscribe(new SingleSubscriber(users.get(i), subscription))
        }
    when:
        def found = subscriberTree.matches(TopicName.valueOf(topicName))
    then:
        found ==~ expectedSubscribers
    where:
        topicName << [
            "/topic/segment1/segment2",
            "/topic/segment3",
            "/topic/segment2/"
        ]
        //noinspection GroovyAssignabilityCheck
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
        //noinspection GroovyAssignabilityCheck
        users << [
            [
                makeUser("id1"),
                makeUser("id1"),
                makeUser("id1"),
                makeUser("id2"),
                makeUser("id2"),
                makeUser("id2"),
                makeUser("id3"),
                makeUser("id3"),
                makeUser("id3")
            ],
            [
                makeUser("id1"),
                makeUser("id1"),
                makeUser("id1"),
                makeUser("id2"),
                makeUser("id2"),
                makeUser("id2"),
                makeUser("id3"),
                makeUser("id3"),
                makeUser("id3")
            ],
            [
                makeUser("id1"),
                makeUser("id1"),
                makeUser("id1"),
                makeUser("id2"),
                makeUser("id2"),
                makeUser("id2"),
                makeUser("id3"),
                makeUser("id3"),
                makeUser("id3")
            ]
        ]
        //noinspection GroovyAssignabilityCheck
        expectedSubscribers << [
            [
                createSubscriber("id1", "/topic/segment1/segment2", 2),
                createSubscriber("id2", "/topic/segment1/#", 1),
                createSubscriber("id3", "/topic/#", 0),
            ],
            [
                createSubscriber("id1", "/topic/#", 0),
                createSubscriber("id2", "/topic/#", 0),
                createSubscriber("id3", "/topic/#", 0),
            ],
            [
                createSubscriber("id1", "/topic/#", 0),
                createSubscriber("id2", "/topic/#", 0),
                createSubscriber("id3", "/topic/segment2/#", 1),
            ]
        ]
  }

  def "should provide different owners when math shared topic"() {
    given:
        def group1 = ["id1", "id2", "id3", "id4", "id5"]
        def group2 = ["id6", "id7", "id8", "id9", "id10"]
        ConcurrentSubscriberTree subscriberTree = new ConcurrentSubscriberTree()
        subscriberTree.subscribe(createShareSubscriber("id1", '$share/group1/topic/name1'))
        subscriberTree.subscribe(createShareSubscriber("id2", '$share/group1/topic/name1'))
        subscriberTree.subscribe(createShareSubscriber("id3", '$share/group1/topic/name1'))
        subscriberTree.subscribe(createShareSubscriber("id4", '$share/group1/topic/name1'))
        subscriberTree.subscribe(createShareSubscriber("id5", '$share/group1/topic/name1'))
        subscriberTree.subscribe(createShareSubscriber("id6", '$share/group2/topic/name1'))
        subscriberTree.subscribe(createShareSubscriber("id7", '$share/group2/topic/name1'))
        subscriberTree.subscribe(createShareSubscriber("id8", '$share/group2/topic/name1'))
        subscriberTree.subscribe(createShareSubscriber("id9", '$share/group2/topic/name1'))
        subscriberTree.subscribe(createShareSubscriber("id10", '$share/group2/topic/name1'))
    when:
        def matched = subscriberTree
            .matches(TopicName.valueOf("topic/name1"))
            .collect { it.user().toString() }
    then:
        matched.size() == 2
    when:
        def matched2 = subscriberTree
            .matches(TopicName.valueOf("topic/name1"))
            .collect { it.user().toString() }
    then:
        matched2.size() == 2
        matched2 != matched
    then: "should contains by one owner from different groups"
        (group1.contains(matched[0]) && group2.contains(matched[1])) ||
            (group1.contains(matched[1]) && group2.contains(matched[0]))
        (group1.contains(matched2[0]) && group2.contains(matched2[1])) ||
            (group1.contains(matched2[1]) && group2.contains(matched2[0]))
  }

  def "should subscribe and unsubscribe simple topic correctly correctly"() {
    given:
        ConcurrentSubscriberTree subscriberTree = new ConcurrentSubscriberTree()
        subscriberTree.subscribe(createSubscriber("id1", 'topic/name1'))
        subscriberTree.subscribe(createSubscriber("id2", 'topic/name1'))
        subscriberTree.subscribe(createSubscriber("id3", 'topic/name1'))
    when:
        def matched = subscriberTree
            .matches(TopicName.valueOf("topic/name1"))
            .collect { it.user().toString() }
            .toSet()
    then:
        matched.size() == 3
    when:
        def id2WasUnsubscribed = subscriberTree.unsubscribe(makeUser("id2"), TopicFilter.valueOf('topic/name1'))
        def id3WasUnsubscribed = subscriberTree.unsubscribe(makeUser("id3"), TopicFilter.valueOf('topic/name1'))
        matched = subscriberTree
            .matches(TopicName.valueOf("topic/name1"))
            .collect { it.user().toString() }
            .toSet()
    then:
        matched.size() == 1
        id2WasUnsubscribed
        id3WasUnsubscribed
    when:
        def id1WasUnsubscribed = subscriberTree.unsubscribe(makeUser("id1"), TopicFilter.valueOf('topic/name1'))
        id3WasUnsubscribed = subscriberTree.unsubscribe(makeUser("id3"), TopicFilter.valueOf('topic/name1'))
        matched = subscriberTree
            .matches(TopicName.valueOf("topic/name1"))
            .collect { it.user().toString() }
            .toSet()
    then:
        matched.size() == 0
        id1WasUnsubscribed
        !id3WasUnsubscribed
  }

  def "should subscribe and unsubscribe shared topic correctly correctly"() {
    given:
        ConcurrentSubscriberTree subscriberTree = new ConcurrentSubscriberTree()
        subscriberTree.subscribe(createShareSubscriber("id1", '$share/group1/topic/name1'))
        subscriberTree.subscribe(createShareSubscriber("id2", '$share/group1/topic/name1'))
        subscriberTree.subscribe(createShareSubscriber("id3", '$share/group1/topic/name1'))
    when:
        def matched = subscriberTree
            .matches(TopicName.valueOf("topic/name1"))
            .collect { it.user().toString() }
            .toSet()
    then:
        matched.size() == 1
    when:
        def id2WasUnsubscribed = subscriberTree.unsubscribe(
            makeUser("id2"),
            SharedTopicFilter.valueOf('$share/group1/topic/name1'))
        def id3WasUnsubscribed = subscriberTree.unsubscribe(
            makeUser("id3"),
            SharedTopicFilter.valueOf('$share/group1/topic/name1'))
        matched = subscriberTree
            .matches(TopicName.valueOf("topic/name1"))
            .collect { it.user().toString() }
            .toSet()
    then:
        matched.size() == 1
        id2WasUnsubscribed
        id3WasUnsubscribed
    when:
        def id1WasUnsubscribed = subscriberTree.unsubscribe(
            makeUser("id1"),
            SharedTopicFilter.valueOf('$share/group1/topic/name1'))
        id3WasUnsubscribed = subscriberTree.unsubscribe(
            makeUser("id3"),
            SharedTopicFilter.valueOf('$share/group1/topic/name1'))
        matched = subscriberTree
            .matches(TopicName.valueOf("topic/name1"))
            .collect { it.user().toString() }
            .toSet()
    then:
        matched.size() == 0
        id1WasUnsubscribed
        !id3WasUnsubscribed
  }

  def "should replace the same subscriptions"() {
    given:
        ConcurrentSubscriberTree subscriberTree = new ConcurrentSubscriberTree()
        def owner1 = makeUser("id1")
        def originalSub = makeSubscription('topic/name1')
        def replacementSub = makeSubscription('topic/name1')
        subscriberTree.subscribe(createSubscriber("id2", 'topic/name1'))
        subscriberTree.subscribe(createSubscriber("id3", 'topic/name1'))
    when:
        def previous = subscriberTree.subscribe(new SingleSubscriber(owner1, originalSub))
        def matched = subscriberTree
            .matches(TopicName.valueOf("topic/name1"))
            .toSet()
    then:
        matched.size() == 3
        previous == null;
    when:
        previous = subscriberTree.subscribe(new SingleSubscriber(owner1, replacementSub))
        matched = subscriberTree
            .matches(TopicName.valueOf("topic/name1"))
            .toSet()
    then:
        matched.size() == 3
        matched.first().subscription() == replacementSub
        previous != null
        previous.subscription() == originalSub
  }

  def "should extend shared subscription group on multiply subscribing by the same topic"() {
    given:
        ConcurrentSubscriberTree subscriberTree = new ConcurrentSubscriberTree()
        def owner1 = makeUser("id1")
        def owner2 = makeUser("id2")
        subscriberTree.subscribe(new SingleSubscriber(owner1, makeSharedSubscription('$share/group1/topic/name1')))
        subscriberTree.subscribe(new SingleSubscriber(owner2, makeSharedSubscription('$share/group1/topic/name1')))
    when:
        def matched = subscriberTree
            .matches(TopicName.valueOf("topic/name1"))
            .toSet()
    then:
        matched.size() == 1
        matched.first().user() == owner2
    when:
        matched = subscriberTree
            .matches(TopicName.valueOf("topic/name1"))
            .toSet()
    then:
        matched.size() == 1
        matched.first().user() == owner1
    when:
        matched = subscriberTree
            .matches(TopicName.valueOf("topic/name1"))
            .toSet()
    then:
        matched.size() == 1
        matched.first().user() == owner2
    when:
        subscriberTree.subscribe(new SingleSubscriber(owner1, makeSharedSubscription('$share/group1/topic/name1')))
        matched = subscriberTree
            .matches(TopicName.valueOf("topic/name1"))
            .toSet()
    then:
        matched.size() == 1
        matched.first().user() == owner2
    when:
        matched = subscriberTree
            .matches(TopicName.valueOf("topic/name1"))
            .toSet()
    then:
        matched.size() == 1
        matched.first().user() == owner1
    when:
        matched = subscriberTree
            .matches(TopicName.valueOf("topic/name1"))
            .toSet()
    then:
        matched.size() == 1
        matched.first().user() == owner1
  }

  static def makeUser(String id) {
    return new TestMqttUser(id)
  }

  static def makeSubscription(String topicFilter) {
    return new Subscription(
        TopicFilter.valueOf(topicFilter),
        MqttProperties.SUBSCRIPTION_ID_IS_NOT_SET,
        QoS.AT_LEAST_ONCE,
        SubscribeRetainHandling.SEND,
        true,
        true)
  }

  static def makeSharedSubscription(String topicFilter) {
    return new Subscription(
        SharedTopicFilter.valueOf(topicFilter),
        MqttProperties.SUBSCRIPTION_ID_IS_NOT_SET,
        QoS.AT_LEAST_ONCE,
        SubscribeRetainHandling.SEND,
        true,
        true)
  }

  static def makeSubscription(String topicFilter, int qos) {
    return new Subscription(
        TopicFilter.valueOf(topicFilter),
        MqttProperties.SUBSCRIPTION_ID_IS_NOT_SET,
        QoS.ofCode(qos),
        SubscribeRetainHandling.SEND,
        true,
        true)
  }
}
