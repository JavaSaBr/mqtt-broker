package javasabr.mqtt.model.acl.rule

import javasabr.mqtt.model.acl.Operation
import javasabr.mqtt.model.subscription.TestMqttUser
import javasabr.mqtt.service.acl.ValueMatchersAware
import javasabr.mqtt.test.support.UnitSpecification

import static javasabr.mqtt.model.acl.Operation.PUBLISH
import static javasabr.mqtt.model.acl.Operation.SUBSCRIBE

class RuleTest extends UnitSpecification implements ValueMatchersAware {

  def "should test rule"(Rule rule, Operation operation, boolean expectedResult) {
    given:
        def user = new TestMqttUser("clientId", null, null)
        def topic = "topic"
    when:
        boolean result = rule.test(user, operation, topic)
    then:
        result == expectedResult
    where:
        operation | rule                                               | expectedResult
        PUBLISH   | new AllowPublishRule(clientIdEquals("clientId"))   | true
        SUBSCRIBE | new AllowPublishRule(clientIdEquals("clientId"))   | false
        PUBLISH   | new AllowSubscribeRule(clientIdEquals("clientId")) | false
        SUBSCRIBE | new AllowSubscribeRule(clientIdEquals("clientId")) | true
        PUBLISH   | new DenyPublishRule(clientIdEquals("clientId"))    | true
        SUBSCRIBE | new DenyPublishRule(clientIdEquals("clientId"))    | false
        PUBLISH   | new DenySubscribeRule(clientIdEquals("clientId"))  | false
        SUBSCRIBE | new DenySubscribeRule(clientIdEquals("clientId"))  | true
  }
}
