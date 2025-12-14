package javasabr.mqtt.acl.engine.model.rule

import javasabr.mqtt.model.acl.Operation
import TopicCondition
import TestMqttUser
import javasabr.mqtt.model.topic.TopicName
import ConditionMatcherAware
import javasabr.mqtt.test.support.UnitSpecification

import static javasabr.mqtt.model.acl.Operation.PUBLISH
import static javasabr.mqtt.model.acl.Operation.SUBSCRIBE

class RuleTest extends UnitSpecification implements ConditionMatcherAware {

  def "should test rule"(Rule rule, Operation operation, boolean expectedResult) {
    given:
        def user = new TestMqttUser("clientId")
        def topic = TopicName.valueOf("topic")
    when:
        boolean result = rule.test(user, operation, topic)
    then:
        result == expectedResult
    where:
        operation | rule                                                                                | expectedResult
        PUBLISH   | new AllowPublishRule(clientIdEquals("clientId"), TopicCondition.MATCH_ANY)          | true
        SUBSCRIBE | new AllowPublishRule(clientIdEquals("clientId"), TopicCondition.MATCH_ANY)          | false
        PUBLISH   | new AllowSubscribeRule(clientIdEquals("clientId"), TopicCondition.MATCH_ANY)        | false
        SUBSCRIBE | new AllowSubscribeRule(clientIdEquals("clientId"), TopicCondition.MATCH_ANY)        | true
        PUBLISH   | new DenyPublishRule(clientIdEquals("clientId"), TopicCondition.MATCH_ANY)           | true
        SUBSCRIBE | new DenyPublishRule(clientIdEquals("clientId"), TopicCondition.MATCH_ANY)           | false
        PUBLISH   | new DenySubscribeRule(clientIdEquals("clientId"), TopicCondition.MATCH_ANY)         | false
        SUBSCRIBE | new DenySubscribeRule(clientIdEquals("clientId"), TopicCondition.MATCH_ANY)         | true
        PUBLISH   | new AllowPublishRule(clientIdEquals("clientId"), topicNameCondition("other/topic")) | false
        PUBLISH   | new AllowPublishRule(clientIdEquals("otherClientId"), TopicCondition.MATCH_ANY)     | false
  }
}
