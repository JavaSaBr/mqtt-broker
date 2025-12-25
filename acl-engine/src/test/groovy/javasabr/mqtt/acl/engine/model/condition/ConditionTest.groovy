package javasabr.mqtt.acl.engine.model.condition

import javasabr.mqtt.acl.engine.ConditionMatcherAware
import javasabr.mqtt.acl.engine.model.matcher.AnyValueMatcher
import javasabr.mqtt.acl.engine.model.matcher.ValueMatcher
import javasabr.mqtt.model.MqttUser
import javasabr.mqtt.model.subscription.TestMqttUser
import javasabr.mqtt.model.topic.TopicName
import javasabr.mqtt.test.support.UnitSpecification
import javasabr.rlib.collections.array.Array

class ConditionTest extends UnitSpecification implements ConditionMatcherAware {

  def "should test user condition"(MqttUserCondition condition, MqttUser mqttUser, boolean expectedResult) {
    when:
        boolean result = condition.test(mqttUser)
    then:
        result == expectedResult
    where:
        mqttUser                                  | condition                                                                | expectedResult
        new TestMqttUser(null, "username2", null) | new AnyOfCondition(userNameEquals("username2"))                          | true
        new TestMqttUser(null, "username2", null) | new AllOfCondition(userNameEquals("username2"))                          | true
        new TestMqttUser(null, "username2", null) | new AnyOfCondition(userNameEquals("username1"))                          | false
        new TestMqttUser(null, "username2", null) | new AllOfCondition(userNameEquals("username1"))                          | false
        new TestMqttUser("username2", null, null) | clientIdEquals("username2")                                              | true
        new TestMqttUser(null, null, "username2") | ipAddressEquals("username2")                                             | true
        new TestMqttUser("username", null, null)  | clientIdEquals("username1")                                              | false
        new TestMqttUser(null, null, "username")  | ipAddressEquals("username1")                                             | false
        new TestMqttUser(null, null, null)        | new AnyUserCondition()                                                   | true
        new TestMqttUser(null, null, null)        | new AnyOfCondition(new UserNameCondition(ValueMatcher.MATCH_ANY_STRING)) | true
  }

  def "should test topic condition"(TopicCondition condition, TopicName mqttUser, boolean expectedResult) {
    when:
        boolean result = condition.test(mqttUser)
    then:
        result == expectedResult
    where:
        mqttUser                   | condition                      | expectedResult
        TopicName.valueOf("topic") | topicNameCondition("topic")    | true
        TopicName.valueOf("topic") | topicNameCondition("topic1")   | false
        TopicName.valueOf("topic") | new TopicCondition(Array.of()) | false
  }
}
