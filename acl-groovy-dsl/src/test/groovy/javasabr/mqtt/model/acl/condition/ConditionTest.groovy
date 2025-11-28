package javasabr.mqtt.model.acl.condition

import javasabr.mqtt.model.acl.CallId
import javasabr.mqtt.service.acl.ValueMatchersAware
import javasabr.mqtt.test.support.UnitSpecification

class ConditionTest extends UnitSpecification implements ValueMatchersAware {

  def "should test condition"(Condition topicFilter, CallId callId, boolean expectedResult) {
    when:
        boolean result = topicFilter.test(callId)
    then:
        result == expectedResult
    where:
        callId                                         | topicFilter                                     | expectedResult
        new CallId("username", null, null, null, null) | new AnyOfCondition(userNameEquals("username"))  | true
        new CallId("username", null, null, null, null) | new AllOfCondition(userNameEquals("username"))  | true
        new CallId("username", null, null, null, null) | new AnyOfCondition(userNameEquals("username1")) | false
        new CallId("username", null, null, null, null) | new AllOfCondition(userNameEquals("username1")) | false
        new CallId(null, "username", null, null, null) | clientIdEquals("username")                      | true
        new CallId(null, null, "username", null, null) | ipAddressEquals("username")                     | true
        new CallId(null, "username", null, null, null) | clientIdEquals("username1")                     | false
        new CallId(null, null, "username", null, null) | ipAddressEquals("username1")                    | false
        new CallId(null, null, null, null, null)       | new AnyCondition()                              | true
  }
}
