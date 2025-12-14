package javasabr.mqtt.acl.engine

import javasabr.mqtt.acl.engine.builder.TopicMatcherBuilder
import javasabr.mqtt.acl.engine.model.condition.AllOfCondition
import javasabr.mqtt.acl.engine.model.condition.AnyOfCondition
import javasabr.mqtt.acl.engine.model.condition.MqttUserCondition
import javasabr.mqtt.acl.engine.model.condition.TopicCondition
import javasabr.mqtt.acl.engine.model.matcher.TopicNameMatcher
import javasabr.mqtt.acl.engine.model.rule.AllowPublishRule
import javasabr.mqtt.acl.engine.model.rule.AllowSubscribeRule
import javasabr.mqtt.acl.engine.model.rule.DenyPublishRule
import javasabr.mqtt.acl.engine.model.rule.DenySubscribeRule
import javasabr.mqtt.acl.engine.model.rule.Rule
import javasabr.mqtt.model.MqttUser
import javasabr.mqtt.model.acl.Operation
import javasabr.mqtt.model.topic.AbstractTopic
import javasabr.mqtt.model.topic.TopicFilter
import javasabr.mqtt.model.topic.TopicName
import javasabr.mqtt.test.support.UnitSpecification
import javasabr.rlib.collections.array.Array
import javasabr.rlib.collections.array.MutableArray

import static javasabr.mqtt.model.acl.Operation.PUBLISH
import static javasabr.mqtt.model.acl.Operation.SUBSCRIBE

class AclEngineTest extends UnitSpecification implements ConditionMatcherAware, TopicMatcherBuilder {

  def "should allow or deny according rules"(
      String username, String clientId, String ipAddress, Operation operation, AbstractTopic topic) {
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
                new TopicNameMatcher(TopicName.valueOf("/topic1/#")),
                new TopicNameMatcher(TopicName.valueOf("/topic2/+/temp"))
            ))
        )
        publishRules << new DenyPublishRule(
            new AllOfCondition(
                userNameEquals("user10"),
                clientIdEquals("clientId1"),
                ipAddressEquals("12.30.0.117"),
            ),
            new TopicCondition(Array.of(
                new TopicNameMatcher(TopicName.valueOf("/topic/home/temp"))
            ))
        )
        publishRules << new AllowPublishRule(
            new AnyOfCondition(
                userNameEquals("user120"),
                clientIdEquals("clientId500"),
                ipAddressEquals("12.30.0.117"),
            ),
            new TopicCondition(Array.of(
                new TopicNameMatcher(TopicName.valueOf("/topic/home/temp"))
            ))
        )
        publishRules << new AllowPublishRule(MqttUserCondition.MATCH_ANY, new TopicCondition(Array.of(
            new TopicNameMatcher(TopicName.valueOf("/topic1/#")),
            new TopicNameMatcher(TopicName.valueOf("/topic2/+/temp"))
        )))
    and:
        Array<Rule> subscribeRules = MutableArray.ofType(Rule.class)
        rulesEnumMap.put(SUBSCRIBE, subscribeRules)
        subscribeRules << new DenySubscribeRule(MqttUserCondition.MATCH_ANY, new TopicCondition(Array.of(
            match("/allowed/+/restricted")
        )))
        subscribeRules << new AllowSubscribeRule(new AllOfCondition(
            userNameEquals("admin"),
            clientIdEquals("id"),
            ipAddressEquals("10.0.0.1"),
        ), new TopicCondition(Array.of(
            match("/allowed/#")
        )))
        subscribeRules << new DenySubscribeRule(MqttUserCondition.MATCH_ANY, new TopicCondition(Array.of(
            match("\$SYS/#"),
            match("#")
        )))
        subscribeRules << new AllowSubscribeRule(MqttUserCondition.MATCH_ANY, TopicCondition.MATCH_ANY)
    and:
        AclEngine engine = new AclEngine(rulesEnumMap)
        MqttUser mqttUser = Mock(MqttUser)
        mqttUser.userName() >> username
        mqttUser.clientId() >> clientId
        mqttUser.ipAddress() >> ipAddress
    when:
        boolean result = engine.authorize(mqttUser, operation, topic)
    then:
        result == expectedResult
    where:
        username   | clientId    | ipAddress     | operation | topic                                           | expectedResult
        "sensor1"  | "clientId2" | "60.50.0.1"   | PUBLISH   | TopicName.valueOf("/topic1/#")                  | true
        "sensor2"  | "clientId1" | "60.50.0.1"   | PUBLISH   | TopicName.valueOf("/topic1/#")                  | true
        "sensor2"  | "clientId2" | "127.0.0.1"   | PUBLISH   | TopicName.valueOf("/topic1/#")                  | true
        "sensor2"  | "clientId2" | "127.0.0.2"   | PUBLISH   | TopicName.valueOf("/topic1/#")                  | true
        "sensor2"  | "clientId2" | "127.0.0.2"   | PUBLISH   | TopicName.valueOf("/topic/#")                   | false
        "user10"   | "clientId1" | "12.30.0.117" | PUBLISH   | TopicName.valueOf("/topic/home/temp")           | false
        "user10"   | "clientId1" | "12.30.0.117" | PUBLISH   | TopicName.valueOf("/topic1/#")                  | true
        "user10"   | "clientId1" | "12.30.0.117" | PUBLISH   | TopicName.valueOf("/topic/home/temp")           | false
        "user120"  | "clientId1" | "12.30.0.117" | PUBLISH   | TopicName.valueOf("/topic/home/temp")           | true
        "sensorX"  | "id"        | "1.1.1.1"     | PUBLISH   | TopicName.valueOf("/topic1/data")               | false
        "sensors1" | "id"        | "1.1.1.1"     | PUBLISH   | TopicName.valueOf("/topic1/data")               | false
        "sensor1"  | "id"        | "1.1.1.1"     | PUBLISH   | TopicName.valueOf("/topic2/temp")               | false
        "sensor1/" | "id"        | "1.1.1.1"     | PUBLISH   | TopicName.valueOf("/topic1/#")                  | true
        "user10"   | "clientId1" | "12.30.0.117" | PUBLISH   | TopicName.valueOf("/topic/home/temp")           | false
        "user10"   | "clientId2" | "12.30.0.117" | PUBLISH   | TopicName.valueOf("/topic/home/temp")           | true
        "nobody"   | "none"      | "0.0.0.0"     | PUBLISH   | TopicName.valueOf("/unknown/topic")             | false
        "admin"    | "id"        | "10.0.0.1"    | SUBSCRIBE | TopicFilter.valueOf("system/status")            | false
        "admin"    | "id"        | "10.0.0.1"    | SUBSCRIBE | TopicFilter.valueOf("\$SYS/info")               | false
        "admin"    | "id"        | "10.0.0.1"    | SUBSCRIBE | TopicFilter.valueOf("/allowed/topic")           | true
        "admin"    | "id"        | "10.0.0.1"    | SUBSCRIBE | TopicFilter.valueOf("/allowed/topic/#")         | true
        "admin"    | "id"        | "10.0.0.1"    | SUBSCRIBE | TopicFilter.valueOf("/allowed/+")               | true
        "admin"    | "id"        | "10.0.0.1"    | SUBSCRIBE | TopicFilter.valueOf("/allowed/+/temp")          | true
        "admin"    | "id"        | "10.0.0.1"    | SUBSCRIBE | TopicFilter.valueOf("/allowed/sub1/restricted") | false
        "admin"    | "id"        | "10.0.0.1"    | SUBSCRIBE | TopicFilter.valueOf("/allowed/+/restricted")    | false
        "username" | "clientId"  | "127.0.0.1"   | SUBSCRIBE | TopicFilter.valueOf("/topic/#")                 | false
        "user"     | "id"        | "10.0.0.10"   | SUBSCRIBE | TopicFilter.valueOf("home/temp/status")         | false
        "user"     | "id"        | "10.0.0.10"   | SUBSCRIBE | TopicFilter.valueOf("\$SYS/info")               | false
        "admin"    | "id"        | "10.0.0.1"    | SUBSCRIBE | TopicFilter.valueOf("/allowed/data")            | true
        "admin"    | "id"        | "10.0.0.1"    | SUBSCRIBE | TopicFilter.valueOf("/allowed/#")               | true
        "admin"    | "id"        | "10.0.0.2"    | SUBSCRIBE | TopicFilter.valueOf("/allowed/data")            | false
        "nobody"   | "id"        | "10.0.0.1"    | SUBSCRIBE | TopicFilter.valueOf("/allowed/data")            | false
        "user"     | "id"        | "10.0.0.1"    | SUBSCRIBE | TopicFilter.valueOf("home/status")              | false
        "admin"    | "id"        | "10.0.0.1"    | SUBSCRIBE | TopicFilter.valueOf("/allowed")                 | true
        "admin"    | "id"        | "10.0.0.1"    | SUBSCRIBE | TopicFilter.valueOf("/allowed/a/b/c/d")         | true
        "admin"    | "id"        | "10.0.0.1"    | SUBSCRIBE | TopicFilter.valueOf("/other/topic")             | false
  }
}
