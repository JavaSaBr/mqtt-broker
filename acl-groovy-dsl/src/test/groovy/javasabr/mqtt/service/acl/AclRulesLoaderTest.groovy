package javasabr.mqtt.service.acl

import javasabr.mqtt.model.acl.Clients
import javasabr.mqtt.model.acl.Rule
import javasabr.mqtt.test.support.UnitSpecification
import javasabr.rlib.collections.array.Array

import static javasabr.mqtt.model.acl.Action.PUBLISH
import static javasabr.mqtt.model.acl.Operator.OR
import static javasabr.mqtt.model.acl.Permission.ALLOW

class AclRulesLoaderTest extends UnitSpecification {

  @SuppressWarnings('GroovyAccessibility')
  def "should parse Groovy DSL config"() {
    when:
        Array<Rule> rules = AclRulesLoader.load()
    then:
        verifyAll(rules) {
          size() == 4
          with(get(0)) {
            name() == "sys_dashboard_sub"
            action() == PUBLISH
            permission() == ALLOW
            with(clients()) {
              operator == OR
              usernames.containsAll("sensor1", "sensor10")
              clientIds.containsAll("clientId1", "sensor10", "/^sensor1/", "/sensor10\$/")
              ipAddresses.containsAll("10.56.0.3", "127.0.0.1")
            }
            topics().containsAll("/topic1/#", "/topic2/+/temp")
          }
          with(get(1)) {
            name() == "sys_dashboard_sub2"
            action() == PUBLISH
            permission() == ALLOW
            clients() == Clients.ALL
            topics().containsAll("/topic1/#", "/topic2/+/temp")
          }
          get(2).name() == "deny_subscribe_all"
          get(3).name() == "allow_all"
        }
  }
}
