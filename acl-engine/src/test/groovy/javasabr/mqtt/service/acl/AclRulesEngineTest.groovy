package javasabr.mqtt.service.acl

import javasabr.mqtt.model.MqttUser
import javasabr.mqtt.model.acl.Operation
import javasabr.mqtt.model.acl.condition.AllOfCondition
import javasabr.mqtt.model.acl.condition.AnyOfCondition
import javasabr.mqtt.model.acl.condition.MqttUserCondition
import javasabr.mqtt.model.acl.condition.TopicCondition
import javasabr.mqtt.model.acl.matcher.EqualsMatcher
import javasabr.mqtt.model.acl.rule.AllowPublishRule
import javasabr.mqtt.model.acl.rule.AllowSubscribeRule
import javasabr.mqtt.model.acl.rule.DenyPublishRule
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
        publishRules << new DenyPublishRule(
            new AllOfCondition(
                userNameEquals("user10"),
                clientIdEquals("clientId1"),
                ipAddressEquals("12.30.0.117"),
            ),
            new TopicCondition(Array.of(
                new EqualsMatcher("/topic/home/temp")
            ))
        )
        publishRules << new AllowPublishRule(
            new AnyOfCondition(
                userNameEquals("user120"),
                clientIdEquals("clientId500"),
                ipAddressEquals("12.30.0.117"),
            ),
            new TopicCondition(Array.of(
                new EqualsMatcher("/topic/home/temp")
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
            topicFilterMatcher("/allowed/+/restricted")
        )))
        subscribeRules << new AllowSubscribeRule(new AllOfCondition(
            userNameEquals("admin"),
            clientIdEquals("id"),
            ipAddressEquals("10.0.0.1"),
        ), new TopicCondition(Array.of(
            topicFilterMatcher("/allowed/#")
        )))
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
        username   | clientId    | ipAddress     | operation | topic                      | expectedResult
        "sensor1"  | "clientId2" | "60.50.0.1"   | PUBLISH   | "/topic1/#"                | true
        "sensor2"  | "clientId1" | "60.50.0.1"   | PUBLISH   | "/topic1/#"                | true
        "sensor2"  | "clientId2" | "127.0.0.1"   | PUBLISH   | "/topic1/#"                | true
        "sensor2"  | "clientId2" | "127.0.0.2"   | PUBLISH   | "/topic1/#"                | true
        "sensor2"  | "clientId2" | "127.0.0.2"   | PUBLISH   | "/topic/#"                 | false
        "user10"   | "clientId1" | "12.30.0.117" | PUBLISH   | "/topic/home/temp"         | false
        "user10"   | "clientId1" | "12.30.0.117" | PUBLISH   | "/topic1/#"                | true
        "user10"   | "clientId1" | "12.30.0.117" | PUBLISH   | "/topic/home/temp"         | false
        "user120"  | "clientId1" | "12.30.0.117" | PUBLISH   | "/topic/home/temp"         | true
        "sensorX"  | "id"        | "1.1.1.1"     | PUBLISH   | "/topic1/data"             | false
        "sensors1" | "id"        | "1.1.1.1"     | PUBLISH   | "/topic1/data"             | false
        "sensor1"  | "id"        | "1.1.1.1"     | PUBLISH   | "/topic2/temp"             | false
        "sensor1/" | "id"        | "1.1.1.1"     | PUBLISH   | "/topic1/#"                | true
        "user10"   | "clientId1" | "12.30.0.117" | PUBLISH   | "/topic/home/temp"         | false
        "user10"   | "clientId2" | "12.30.0.117" | PUBLISH   | "/topic/home/temp"         | true
        "nobody"   | "none"      | "0.0.0.0"     | PUBLISH   | "/unknown/topic"           | false
        "admin"    | "id"        | "10.0.0.1"    | SUBSCRIBE | "system/status"            | false
        "admin"    | "id"        | "10.0.0.1"    | SUBSCRIBE | "\$SYS/info"               | false
        "admin"    | "id"        | "10.0.0.1"    | SUBSCRIBE | "/allowed/topic"           | true
        "admin"    | "id"        | "10.0.0.1"    | SUBSCRIBE | "/allowed/topic/#"         | true
        "admin"    | "id"        | "10.0.0.1"    | SUBSCRIBE | "/allowed/+"               | true
        "admin"    | "id"        | "10.0.0.1"    | SUBSCRIBE | "/allowed/+/temp"          | true
        "admin"    | "id"        | "10.0.0.1"    | SUBSCRIBE | "/allowed/sub1/restricted" | false
        "admin"    | "id"        | "10.0.0.1"    | SUBSCRIBE | "/allowed/+/restricted"    | false
        "username" | "clientId"  | "127.0.0.1"   | SUBSCRIBE | "/topic/#"                 | false
        "user"     | "id"        | "10.0.0.10"   | SUBSCRIBE | "home/temp/status"         | false
        "user"     | "id"        | "10.0.0.10"   | SUBSCRIBE | "\$SYS/info"               | false
        "admin"    | "id"        | "10.0.0.1"    | SUBSCRIBE | "/allowed/data"            | true
        "admin"    | "id"        | "10.0.0.1"    | SUBSCRIBE | "/allowed/#"               | true
        "admin"    | "id"        | "10.0.0.2"    | SUBSCRIBE | "/allowed/data"            | false
        "nobody"   | "id"        | "10.0.0.1"    | SUBSCRIBE | "/allowed/data"            | false
        "user"     | "id"        | "10.0.0.1"    | SUBSCRIBE | "home/status"              | false
        "admin"    | "id"        | "10.0.0.1"    | SUBSCRIBE | "/allowed"                 | true
        "admin"    | "id"        | "10.0.0.1"    | SUBSCRIBE | "/allowed/a/b/c/d"         | true
        "admin"    | "id"        | "10.0.0.1"    | SUBSCRIBE | "/other/topic"             | false
  }
}
