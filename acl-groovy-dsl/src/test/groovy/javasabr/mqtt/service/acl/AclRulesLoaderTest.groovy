package javasabr.mqtt.service.acl

import javasabr.mqtt.model.acl.Action
import javasabr.mqtt.model.acl.ClientMatcher
import javasabr.mqtt.model.acl.Permission
import javasabr.mqtt.model.acl.Rule
import javasabr.mqtt.test.support.UnitSpecification
import javasabr.rlib.collections.array.Array

import static java.lang.reflect.Proxy.getInvocationHandler
import static javasabr.mqtt.model.acl.Action.PUBLISH
import static javasabr.mqtt.model.acl.Permission.ALLOW

class AclRulesLoaderTest extends UnitSpecification implements RegexComparatorBuilder, EqualsComparatorBuilder, ClientMatcherBuilder {

  @SuppressWarnings('GroovyAccessibility')
  def "should parse new Groovy DSL config"() {
    when:
        def configAbsolutePath = Objects.requireNonNull(
            getClass().getClassLoader().getResource("acl.groovy")
        ).toURI()
        Array<Rule> rules = new AclRulesLoader(configAbsolutePath).load()
    then:
        verifyAll(rules) {
          size() == 2

          with(get(0)) {
            action() == PUBLISH
            permission() == ALLOW
            with(clients()) {
              with(it['clientMatchers'] as Array<ClientMatcher>) {
                with(it[0]) {
                  getterName(it) == "username"
                  with(ruleMatchers()) {
                    it[0].rulePattern == "sensor1"
                    it[1].rulePattern.pattern() == "/sensor10\$/"
                  }
                }
                with(it[1]) {
                  getterName(it) == "clientId"
                  with(ruleMatchers()) {
                    it[0].rulePattern == "clientId1"
                    it[1].rulePattern.pattern() == "/^cliend/"
                  }
                }
                with(it[2]) {
                  getterName(it) == "ipAddress"
                  with(ruleMatchers()) {
                    it[0].rulePattern == "10.56.0.3"
                    it[1].rulePattern == "127.0.0.1"
                  }
                }
              }
            }
            topics().containsAll("/topic1", "/topic2/temp")
          }

          with(get(1)) {
            action() == Action.SUBSCRIBE
            permission() == Permission.DENY
            with(clients()) {
              with(it['clientMatchers'] as Array<ClientMatcher>) {
                with(it[0]) {
                  getterName(it) == "username"
                  with(ruleMatchers()) {
                    it[0].rulePattern == "sensor2"
                    it[1].rulePattern.pattern() == "/sensor11\$/"
                  }
                }
                with(it[1]) {
                  getterName(it) == "clientId"
                  with(ruleMatchers()) {
                    it[0].rulePattern == "clientId2"
                    it[1].rulePattern.pattern() == "/^cliend1/"
                  }
                }
                with(it[2]) {
                  getterName(it) == "ipAddress"
                  with(ruleMatchers()) {
                    it[0].rulePattern == "10.56.0.3"
                    it[1].rulePattern == "127.0.0.1"
                  }
                }
              }
            }
            topics().containsAll("/topic1/#", "/topic2/+/temp")
          }
        }
  }

  def getterName(def proxy) {
    return getInvocationHandler(proxy.valueGetter()).delegate.method
  }
}
