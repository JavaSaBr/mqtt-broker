package javasabr.mqtt.service.acl

import javasabr.mqtt.model.acl.Action
import javasabr.mqtt.model.acl.AnyClient
import javasabr.mqtt.model.acl.CallId
import javasabr.mqtt.model.acl.Rule
import javasabr.mqtt.test.support.UnitSpecification
import javasabr.rlib.collections.array.Array
import javasabr.rlib.collections.array.MutableArray

import static javasabr.mqtt.model.acl.Action.ALL
import static javasabr.mqtt.model.acl.Action.PUBLISH
import static javasabr.mqtt.model.acl.Action.SUBSCRIBE
import static javasabr.mqtt.model.acl.AllClients.MATCH_ALL
import static javasabr.mqtt.model.acl.Permission.ALLOW
import static javasabr.mqtt.model.acl.Permission.DENY

class AclRulesEngineTest extends UnitSpecification implements RegexComparatorBuilder, EqualsComparatorBuilder, ClientMatcherBuilder {

  def "should"(String username, String clientId, String ipAddress, Action action, String topic) {
    given:
        Array<Rule> rules = MutableArray.ofType(Rule.class)
        rules << new Rule(ALLOW, PUBLISH,
            new AnyClient(Array.of(
                match(CallId::username, Array.of(
                    eq("sensor1"),
                    eq("sensor10"),
                    regex("/^sensor1/"),
                    regex("/sensor10\$/")
                )),
                match(CallId::clientId, Array.of(
                    eq("clientId1"),
                    eq("sensor10"),
                    regex("/^sensor1/"),
                    regex("/sensor10\$/")
                )),
                match(CallId::ipAddress, Array.of(
                    eq("10.56.0.3"),
                    eq("127.0.0.1")
                ))
            )),
            ["/topic1/#", "/topic2/+/temp"]
        )
        rules << new Rule(ALLOW, PUBLISH, MATCH_ALL, ["/topic1/#", "/topic2/+/temp"])
        rules << new Rule(DENY, SUBSCRIBE, MATCH_ALL, ["\$SYS/#", "#"])
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
