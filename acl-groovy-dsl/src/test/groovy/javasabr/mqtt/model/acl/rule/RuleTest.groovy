package javasabr.mqtt.model.acl.rule

import javasabr.mqtt.model.acl.CallId
import javasabr.mqtt.model.acl.condition.AllOfCondition
import javasabr.mqtt.service.acl.ValueMatchersAware
import javasabr.mqtt.test.support.UnitSpecification

import static javasabr.mqtt.model.acl.Operation.PUBLISH
import static javasabr.mqtt.model.acl.Operation.SUBSCRIBE

class RuleTest extends UnitSpecification implements ValueMatchersAware {

  def "should test condition"(Rule rule, CallId callId, boolean expectedResult) {
    when:
        boolean result = rule.test(callId)
    then:
        result == expectedResult
    where:
        callId                                              | rule                                                                   | expectedResult
        new CallId("username", null, null, PUBLISH, null)   | new AllowPublishRule(new AllOfCondition(userNameEquals("username")))   | true
        new CallId("username", null, null, SUBSCRIBE, null) | new AllowPublishRule(new AllOfCondition(userNameEquals("username")))   | false
        new CallId("username", null, null, PUBLISH, null)   | new AllowSubscribeRule(new AllOfCondition(userNameEquals("username"))) | false
        new CallId("username", null, null, SUBSCRIBE, null) | new AllowSubscribeRule(new AllOfCondition(userNameEquals("username"))) | true
        new CallId("username", null, null, PUBLISH, null)   | new DenyPublishRule(new AllOfCondition(userNameEquals("username")))    | true
        new CallId("username", null, null, SUBSCRIBE, null) | new DenyPublishRule(new AllOfCondition(userNameEquals("username")))    | false
        new CallId("username", null, null, PUBLISH, null)   | new DenySubscribeRule(new AllOfCondition(userNameEquals("username")))  | false
        new CallId("username", null, null, SUBSCRIBE, null) | new DenySubscribeRule(new AllOfCondition(userNameEquals("username")))  | true
  }
}
