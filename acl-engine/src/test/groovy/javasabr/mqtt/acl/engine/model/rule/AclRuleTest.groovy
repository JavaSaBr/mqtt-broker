package javasabr.mqtt.acl.engine.model.rule

import javasabr.mqtt.acl.engine.ConditionMatcherAware
import javasabr.mqtt.acl.engine.model.condition.TopicCondition
import javasabr.mqtt.model.acl.Operation
import javasabr.mqtt.model.subscription.TestMqttUser
import javasabr.mqtt.model.topic.TopicName
import javasabr.mqtt.test.support.UnitSpecification

import static javasabr.mqtt.model.acl.Operation.PUBLISH
import static javasabr.mqtt.model.acl.Operation.SUBSCRIBE

class AclRuleTest extends UnitSpecification implements ConditionMatcherAware {

  def "should test rule"(AclRule rule, Operation operation, boolean expectedResult) {
    given:
        def user = new TestMqttUser("clientId")
        def topic = TopicName.valueOf("topic")
    when:
        boolean result = rule.test(user, operation, topic)
    then:
        result == expectedResult
    where:
        operation | rule                                                                                   | expectedResult
        PUBLISH   | new AllowPublishAclRule(clientIdEquals("clientId"), TopicCondition.MATCH_ANY)          | true
        SUBSCRIBE | new AllowPublishAclRule(clientIdEquals("clientId"), TopicCondition.MATCH_ANY)          | false
        PUBLISH   | new AllowSubscribeAclRule(clientIdEquals("clientId"), TopicCondition.MATCH_ANY)        | false
        SUBSCRIBE | new AllowSubscribeAclRule(clientIdEquals("clientId"), TopicCondition.MATCH_ANY)        | true
        PUBLISH   | new DenyPublishAclRule(clientIdEquals("clientId"), TopicCondition.MATCH_ANY)           | true
        SUBSCRIBE | new DenyPublishAclRule(clientIdEquals("clientId"), TopicCondition.MATCH_ANY)           | false
        PUBLISH   | new DenySubscribeAclRule(clientIdEquals("clientId"), TopicCondition.MATCH_ANY)         | false
        SUBSCRIBE | new DenySubscribeAclRule(clientIdEquals("clientId"), TopicCondition.MATCH_ANY)         | true
        PUBLISH   | new AllowPublishAclRule(clientIdEquals("clientId"), topicNameCondition("other/topic")) | false
        PUBLISH   | new AllowPublishAclRule(clientIdEquals("otherClientId"), TopicCondition.MATCH_ANY)     | false
  }
}
