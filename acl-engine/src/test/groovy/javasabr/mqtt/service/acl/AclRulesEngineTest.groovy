package javasabr.mqtt.service.acl

import javasabr.mqtt.model.acl.Action
import javasabr.mqtt.model.acl.CallId
import javasabr.mqtt.model.acl.Rule
import javasabr.mqtt.model.acl.condition.Any
import javasabr.mqtt.model.acl.condition.AnyOf
import javasabr.mqtt.model.acl.condition.ClientIdCondition
import javasabr.mqtt.model.acl.condition.IpAddressCondition
import javasabr.mqtt.model.acl.condition.UserNameCondition
import javasabr.mqtt.model.acl.value.matcher.EqualsValueMatcher
import javasabr.mqtt.model.acl.value.matcher.RegexValueMatcher
import javasabr.mqtt.model.acl.value.matcher.TopicNameValueMatcher
import javasabr.mqtt.test.support.UnitSpecification
import javasabr.rlib.collections.array.Array
import javasabr.rlib.collections.array.MutableArray

import java.util.regex.Pattern

import static javasabr.mqtt.model.acl.Action.ALL
import static javasabr.mqtt.model.acl.Action.PUBLISH
import static javasabr.mqtt.model.acl.Action.SUBSCRIBE
import static javasabr.mqtt.model.acl.Permission.ALLOW
import static javasabr.mqtt.model.acl.Permission.DENY

class AclRulesEngineTest extends UnitSpecification {

  def "should"(String username, String clientId, String ipAddress, Action action, String topic) {
    given:
        Array<Rule> rules = MutableArray.ofType(Rule.class)
        rules << new Rule(ALLOW, PUBLISH,
            new AnyOf(Array.of(
                new UserNameCondition(new EqualsValueMatcher("sensor1")),
                new UserNameCondition(new EqualsValueMatcher("sensor10")),
                    new UserNameCondition(new RegexValueMatcher(Pattern.compile("^sensor1/"))),
                        new UserNameCondition(new RegexValueMatcher(Pattern.compile("/sensor10\$"))),
                new ClientIdCondition(new EqualsValueMatcher("clientId1")),
                new ClientIdCondition(new EqualsValueMatcher("sensor10")),
                new ClientIdCondition(new RegexValueMatcher(Pattern.compile("/^sensor1/"))),
                    new ClientIdCondition(new RegexValueMatcher(Pattern.compile("/sensor10\$"))),
                new IpAddressCondition(new EqualsValueMatcher("10.56.0.3")),
                    new IpAddressCondition(new EqualsValueMatcher("127.0.0.1"))
            )),
            Array.of(
                new TopicNameValueMatcher("/topic1/#"),
                new TopicNameValueMatcher("/topic2/+/temp")
            )
        )
        rules << new Rule(ALLOW, PUBLISH, new Any(), Array.of(
            new TopicNameValueMatcher("/topic1/#"),
            new TopicNameValueMatcher("/topic2/+/temp")
        ))
        rules << new Rule(DENY, SUBSCRIBE, new Any(), Array.of(
            new TopicNameValueMatcher("\$SYS/#"),
            new TopicNameValueMatcher("#")
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
