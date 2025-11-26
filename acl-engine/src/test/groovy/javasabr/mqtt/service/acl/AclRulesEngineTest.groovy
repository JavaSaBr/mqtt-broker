package javasabr.mqtt.service.acl

import javasabr.mqtt.model.acl.Operation
import javasabr.mqtt.model.acl.CallId
import javasabr.mqtt.model.acl.Rule
import javasabr.mqtt.model.acl.condition.AnyCondition
import javasabr.mqtt.model.acl.condition.AnyOfCondition
import javasabr.mqtt.model.acl.condition.ClientIdCondition
import javasabr.mqtt.model.acl.condition.IpAddressCondition
import javasabr.mqtt.model.acl.condition.UserNameCondition
import javasabr.mqtt.model.acl.matcher.EqualsClientMatcher
import javasabr.mqtt.model.acl.matcher.RegexClientMatcher
import javasabr.mqtt.model.acl.matcher.TopicNameMatcher
import javasabr.mqtt.test.support.UnitSpecification
import javasabr.rlib.collections.array.Array
import javasabr.rlib.collections.array.MutableArray

import java.util.regex.Pattern

import static javasabr.mqtt.model.acl.Operation.ALL
import static javasabr.mqtt.model.acl.Operation.PUBLISH
import static javasabr.mqtt.model.acl.Operation.SUBSCRIBE
import static javasabr.mqtt.model.acl.Action.ALLOW
import static javasabr.mqtt.model.acl.Action.DENY

class AclRulesEngineTest extends UnitSpecification {

  def "should"(String username, String clientId, String ipAddress, Operation action, String topic) {
    given:
        Array<Rule> rules = MutableArray.ofType(Rule.class)
        rules << new Rule(ALLOW, PUBLISH,
            new AnyOfCondition(Array.of(
                new UserNameCondition(new EqualsClientMatcher("sensor1")),
                new UserNameCondition(new EqualsClientMatcher("sensor10")),
                new UserNameCondition(new RegexClientMatcher(Pattern.compile("^sensor1/"))),
                new UserNameCondition(new RegexClientMatcher(Pattern.compile("/sensor10\$"))),

                new ClientIdCondition(new EqualsClientMatcher("clientId1")),
                new ClientIdCondition(new EqualsClientMatcher("sensor10")),
                new ClientIdCondition(new RegexClientMatcher(Pattern.compile("/^sensor1/"))),
                new ClientIdCondition(new RegexClientMatcher(Pattern.compile("/sensor10\$"))),

                new IpAddressCondition(new EqualsClientMatcher("10.56.0.3")),
                new IpAddressCondition(new EqualsClientMatcher("127.0.0.1"))
            )),
            Array.of(
                new TopicNameMatcher("/topic1/#"),
                new TopicNameMatcher("/topic2/+/temp")
            )
        )
        rules << new Rule(ALLOW, PUBLISH, new AnyCondition(), Array.of(
            new TopicNameMatcher("/topic1/#"),
            new TopicNameMatcher("/topic2/+/temp")
        ))
        rules << new Rule(DENY, SUBSCRIBE, new AnyCondition(), Array.of(
            new TopicNameMatcher("\$SYS/#"),
            new TopicNameMatcher("#")
        ))
        rules << new Rule(ALLOW, ALL)
        AclRulesEngine engine = new AclRulesEngine(rules)
        CallId callId = new CallId(username, clientId, ipAddress, action, topic)
    when:
        boolean result = engine.authorize(callId)
    then:
        result == expectedResult
    where:
        username   | clientId    | ipAddress   | action    | topic       | expectedResult
        "username" | "clientId"  | "127.0.0.1" | SUBSCRIBE | "/topic/#"  | false
        "sensor1"  | "clientId2" | "60.50.0.1" | PUBLISH   | "/topic1/#" | true
        "sensor2"  | "clientId1" | "60.50.0.1" | PUBLISH   | "/topic1/#" | true
        "sensor2"  | "clientId2" | "127.0.0.1" | PUBLISH   | "/topic1/#" | true
        "sensor1"  | "clientId1" | "127.0.0.1" | ALL       | "/topic1/#" | false
        "sensor2"  | "clientId2" | "127.0.0.2" | PUBLISH   | "/topic1/#" | true
        "sensor2"  | "clientId2" | "127.0.0.2" | PUBLISH   | "/topic/#"  | false
  }
}
