package javasabr.mqtt.model.acl.condition

import javasabr.mqtt.model.MqttUser
import javasabr.mqtt.model.subscription.TestMqttUser
import javasabr.mqtt.service.acl.ValueMatchersAware
import javasabr.mqtt.test.support.UnitSpecification

class ConditionTest extends UnitSpecification implements ValueMatchersAware {

  def "should test condition"(MqttUserCondition condition, MqttUser mqttUser, boolean expectedResult) {
    when:
        boolean result = condition.test(mqttUser)
    then:
        result == expectedResult
    where:
        mqttUser                                  | condition                                       | expectedResult
        new TestMqttUser(null, "username2", null) | new AnyOfCondition(userNameEquals("username2")) | true
        new TestMqttUser(null, "username2", null) | new AllOfCondition(userNameEquals("username2")) | true
        new TestMqttUser(null, "username2", null) | new AnyOfCondition(userNameEquals("username1")) | false
        new TestMqttUser(null, "username2", null) | new AllOfCondition(userNameEquals("username1")) | false
        new TestMqttUser("username2", null, null) | clientIdEquals("username2")                     | true
        new TestMqttUser(null, null, "username2") | ipAddressEquals("username2")                    | true
        new TestMqttUser("username", null, null)  | clientIdEquals("username1")                     | false
        new TestMqttUser(null, null, "username")  | ipAddressEquals("username1")                    | false
        new TestMqttUser(null, null, null)        | new AnyCondition()                              | true
//        new CallId(null, null, null, null, "topic")    | new TopicCondition(new EqualsMatcher("topic"))  | true
//        new CallId(null, null, null, null, "topic")    | new TopicCondition(new EqualsMatcher("topic1")) | false
  }

  def "should test any condition"() {
    given:
        def cond = new AnyCondition()
    when:
        boolean stringTestResult = cond.test("any_value")
    then:
        stringTestResult
    when:
        def mqttUserTestResult = cond.test(new TestMqttUser("any_id"))
    then:
        mqttUserTestResult
    when:
        def identityValue = cond.getIdentityValue(new TestMqttUser("id"))
    then:
        identityValue.isEmpty()
  }
}
