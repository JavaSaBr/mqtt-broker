package javasabr.mqtt.service.acl

import javasabr.mqtt.model.acl.Action
import javasabr.mqtt.model.acl.Rule
import javasabr.mqtt.test.support.UnitSpecification
import javasabr.rlib.collections.array.Array

import static javasabr.mqtt.model.acl.Action.ALL
import static javasabr.mqtt.model.acl.Action.PUBLISH
import static javasabr.mqtt.model.acl.Action.SUBSCRIBE

class AclRulesEngineTest extends UnitSpecification {

  def "should"(String username, String clientId, String ipAddress, Action action, String topic) {
    given:
        Array<Rule> rules = AclRulesLoader.load()
        AclRulesEngine engine = new AclRulesEngine(rules)
    when:
        boolean result = engine.authorize(username, clientId, ipAddress, action, topic)
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
