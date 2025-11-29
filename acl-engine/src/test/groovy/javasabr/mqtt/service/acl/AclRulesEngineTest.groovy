package javasabr.mqtt.service.acl

import javasabr.mqtt.model.MqttUser
import javasabr.mqtt.model.acl.Operation
import javasabr.mqtt.model.acl.condition.AnyOfCondition
import javasabr.mqtt.model.acl.condition.MqttUserCondition
import javasabr.mqtt.model.acl.condition.TopicCondition
import javasabr.mqtt.model.acl.matcher.EqualsMatcher
import javasabr.mqtt.model.acl.rule.AllowPublishRule
import javasabr.mqtt.model.acl.rule.AllowSubscribeRule
import javasabr.mqtt.model.acl.rule.DenySubscribeRule
import javasabr.mqtt.model.acl.rule.Rule
import javasabr.mqtt.test.support.UnitSpecification
import javasabr.rlib.collections.array.Array
import javasabr.rlib.collections.array.MutableArray

import static javasabr.mqtt.model.acl.Operation.PUBLISH
import static javasabr.mqtt.model.acl.Operation.SUBSCRIBE

class AclRulesEngineTest extends UnitSpecification implements ConditionMatcherAware {

  def "should allow or deny according rules"(
      String username, String clientId, String ipAddress, Operation operation, String topic) {
    given:
        EnumMap<Operation, MutableArray<Rule>> rulesEnumMap = new EnumMap<>(Operation.class)
    and:
        Array<Rule> publishRules = MutableArray.ofType(Rule.class)
        rulesEnumMap.put(PUBLISH, publishRules)
        publishRules << new AllowPublishRule(
            new AnyOfCondition(
                userNameEquals("sensor1"),
                userNameEquals("sensor10"),
                userNameRegex("^sensor1/"),
                userNameRegex("/sensor10\$"),
                clientIdEquals("clientId1"),
                clientIdEquals("sensor10"),
                clientIdRegex("/^sensor1/"),
                clientIdRegex("/sensor10\$"),
                ipAddressEquals("10.56.0.3"),
                ipAddressRegex("127.0.0.1")
            ),
            new TopicCondition(Array.of(
                new EqualsMatcher("/topic1/#"),
                new EqualsMatcher("/topic2/+/temp")
            ))
        )
        publishRules << new AllowPublishRule(MqttUserCondition.MATCH_ANY, new TopicCondition(Array.of(
            new EqualsMatcher("/topic1/#"),
            new EqualsMatcher("/topic2/+/temp")
        )))
    and:
        Array<Rule> subscribeRules = MutableArray.ofType(Rule.class)
        rulesEnumMap.put(SUBSCRIBE, subscribeRules)
        subscribeRules << new DenySubscribeRule(MqttUserCondition.MATCH_ANY, new TopicCondition(Array.of(
            topicFilterMatcher("\$SYS/#"),
            topicFilterMatcher("#")
        )))
        subscribeRules << new AllowSubscribeRule(MqttUserCondition.MATCH_ANY, TopicCondition.MATCH_ANY)
    and:
        AclRulesEngine engine = new AclRulesEngine(rulesEnumMap)
        MqttUser mqttUser = Mock(MqttUser)
        mqttUser.userName() >> username
        mqttUser.clientId() >> clientId
        mqttUser.ipAddress() >> ipAddress

    when:
        boolean result = engine.authorize(mqttUser, operation, topic)
    then:
        result == expectedResult
    where:
        username   | clientId    | ipAddress   | operation | topic       | expectedResult
        "username" | "clientId"  | "127.0.0.1" | SUBSCRIBE | "/topic/#"  | false
        "sensor1"  | "clientId2" | "60.50.0.1" | PUBLISH   | "/topic1/#" | true
        "sensor2"  | "clientId1" | "60.50.0.1" | PUBLISH   | "/topic1/#" | true
        "sensor2"  | "clientId2" | "127.0.0.1" | PUBLISH   | "/topic1/#" | true
        "sensor2"  | "clientId2" | "127.0.0.2" | PUBLISH   | "/topic1/#" | true
        "sensor2"  | "clientId2" | "127.0.0.2" | PUBLISH   | "/topic/#"  | false
  }
}
