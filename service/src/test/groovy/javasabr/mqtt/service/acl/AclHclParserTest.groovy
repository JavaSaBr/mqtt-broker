package javasabr.mqtt.service.acl

import javasabr.mqtt.model.acl.Rule
import javasabr.mqtt.test.support.UnitSpecification

import static javasabr.mqtt.model.acl.Action.PUBLISH
import static javasabr.mqtt.model.acl.Operator.OR
import static javasabr.mqtt.model.acl.Permission.ALLOW

class AclHclParserTest extends UnitSpecification {

  def "should parse Groovy DSL config"() {
    given:
        def aclConfigFile = "acl.groovy";
    when:
        List<Rule> rules = AclRulesLoader.load(aclConfigFile)
    then:
        verifyAll(rules) {
          size() == 3
          with(get(0)) {
            name() == "sys_dashboard_sub"
            action() == PUBLISH
            permission() == ALLOW
            with(clients()) {
              operator() == OR
              usernames().containsAll("sensor1", "sensor10", "/^sensor1/", "/sensor10\$/")
              clientIds().containsAll("sensor1", "sensor10", "/^sensor1/", "/sensor10\$/")
              clientAttrs().containsAll("attr_name1", "attr_value1", "attr_name2", "/attr_value\$/")
              ipAddresses().containsAll("10.56.0.3", "127.0.0.1")
            }
            topics().containsAll("/topic1/#", "/topic2/+/temp")
          }

          get(1).name() == "deny_subscribe_all"
          get(2).name() == "allow_all"
        }
  }
}
