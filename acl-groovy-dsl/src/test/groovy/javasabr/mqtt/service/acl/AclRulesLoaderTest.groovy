package javasabr.mqtt.service.acl

import javasabr.mqtt.model.acl.Rule
import javasabr.mqtt.model.acl.condition.AllOfCondition
import javasabr.mqtt.model.acl.condition.AnyOfCondition
import javasabr.mqtt.model.acl.condition.ClientIdCondition
import javasabr.mqtt.model.acl.condition.Condition
import javasabr.mqtt.model.acl.condition.IpAddressCondition
import javasabr.mqtt.model.acl.condition.UserNameCondition
import javasabr.mqtt.model.acl.value.matcher.EqualsValueMatcher
import javasabr.mqtt.model.acl.value.matcher.RegexValueMatcher
import javasabr.mqtt.model.acl.value.matcher.TopicFilterValueMatcher
import javasabr.mqtt.model.acl.value.matcher.TopicNameValueMatcher
import javasabr.mqtt.test.support.UnitSpecification
import javasabr.rlib.collections.array.Array

import static javasabr.mqtt.model.acl.Action.ALLOW
import static javasabr.mqtt.model.acl.Action.DENY
import static javasabr.mqtt.model.acl.Operation.PUBLISH
import static javasabr.mqtt.model.acl.Operation.SUBSCRIBE

class AclRulesLoaderTest extends UnitSpecification {

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
            operation == PUBLISH
            action == ALLOW
            with(condition as AnyOfCondition) {
              with(conditions as Array<Condition>) {
                with(get(0) as UserNameCondition) {
                  with(clientMatcher as EqualsValueMatcher) { expectedValue == "sensor1" }
                }
                with(get(1) as UserNameCondition) {
                  with(clientMatcher as RegexValueMatcher) { pattern.pattern() == "/sensor10\$/" }
                }
                with(get(2) as ClientIdCondition) {
                  with(clientMatcher as EqualsValueMatcher) { expectedValue == "clientId1" }
                }
                with(get(3) as ClientIdCondition) {
                  with(clientMatcher as RegexValueMatcher) { pattern.pattern() == "/^cliend/" }
                }
                with(get(4) as IpAddressCondition) {
                  with(clientMatcher as EqualsValueMatcher) { expectedValue == "10.56.0.3" }
                }
                with(get(5) as IpAddressCondition) {
                  with(clientMatcher as EqualsValueMatcher) { expectedValue == "127.0.0.1" }
                }
                with(get(6) as AllOfCondition) {
                  with(conditions as Array<Condition>) {
                    with(get(0) as UserNameCondition) {
                      with(clientMatcher as EqualsValueMatcher) { expectedValue == "sensor2" }
                    }
                    with(get(1) as ClientIdCondition) {
                      with(clientMatcher as EqualsValueMatcher) { expectedValue == "clientId2" }
                    }
                    with(get(2) as IpAddressCondition) {
                      with(clientMatcher as EqualsValueMatcher) { expectedValue == "10.56.0.3" }
                    }
                  }
                }
              }
            }
            topics().containsAll(
                new TopicNameValueMatcher("/topic1"),
                new TopicNameValueMatcher("/topic2/temp")
            )
          }
          with(get(1)) {
            operation == SUBSCRIBE
            action == DENY
            with(condition as AllOfCondition) {
              with(conditions as Array<Condition>) {
                with(get(0) as UserNameCondition) {
                  with(clientMatcher as EqualsValueMatcher) { expectedValue == "sensor2" }
                }
                with(get(1) as UserNameCondition) {
                  with(clientMatcher as RegexValueMatcher) { pattern.pattern() == "/sensor11\$/" }
                }
                with(get(2) as ClientIdCondition) {
                  with(clientMatcher as EqualsValueMatcher) { expectedValue == "clientId2" }
                }
                with(get(3) as ClientIdCondition) {
                  with(clientMatcher as RegexValueMatcher) { pattern.pattern() == "/^cliend1/" }
                }
                with(get(4) as IpAddressCondition) {
                  with(clientMatcher as EqualsValueMatcher) { expectedValue == "10.56.0.3" }
                }
                with(get(5) as IpAddressCondition) {
                  with(clientMatcher as EqualsValueMatcher) { expectedValue == "127.0.0.1" }
                }
              }
            }
            topics().containsAll(
                new TopicFilterValueMatcher("/topic1/#"),
                new TopicFilterValueMatcher("/topic2/+/temp")
            )
          }
        }
  }
}
