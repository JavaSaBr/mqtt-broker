package javasabr.mqtt.model.acl.condition


import javasabr.mqtt.model.subscription.TestMqttUser
import javasabr.mqtt.model.topic.TopicName
import javasabr.mqtt.service.acl.ConditionMatcherAware
import javasabr.mqtt.service.acl.builder.TopicMatcherBuilder
import javasabr.mqtt.test.support.UnitSpecification
import javasabr.rlib.collections.array.Array

class ConditionTest extends UnitSpecification implements ConditionMatcherAware, TopicMatcherBuilder {

  def "should test condition"(MqttUserCondition condition, Object mqttUser, boolean expectedResult) {
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
        TopicName.valueOf("topic")                | topicCondition("topic")                         | true
        TopicName.valueOf("topic")                | topicCondition("topic1")                        | false
        TopicName.valueOf("topic")                | new TopicCondition(Array.of())                  | false
  }
}
