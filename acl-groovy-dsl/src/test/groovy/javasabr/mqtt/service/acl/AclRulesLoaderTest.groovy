package javasabr.mqtt.service.acl

import javasabr.mqtt.model.acl.condition.AllOfCondition
import javasabr.mqtt.model.acl.condition.AnyOfCondition
import javasabr.mqtt.model.acl.condition.ClientIdCondition
import javasabr.mqtt.model.acl.condition.Condition
import javasabr.mqtt.model.acl.condition.IpAddressCondition
import javasabr.mqtt.model.acl.condition.TopicCondition
import javasabr.mqtt.model.acl.condition.UserNameCondition
import javasabr.mqtt.model.acl.matcher.EqualsMatcher
import javasabr.mqtt.model.acl.matcher.RegexMatcher
import javasabr.mqtt.model.acl.matcher.TopicFilterMatcher
import javasabr.mqtt.model.exception.AclConfigurationException
import javasabr.mqtt.test.support.UnitSpecification
import javasabr.rlib.collections.array.Array

import java.util.concurrent.CompletionException

import static javasabr.mqtt.model.acl.Action.ALLOW
import static javasabr.mqtt.model.acl.Action.DENY
import static javasabr.mqtt.model.acl.Operation.PUBLISH
import static javasabr.mqtt.model.acl.Operation.SUBSCRIBE

class AclRulesLoaderTest extends UnitSpecification {

  def "should load test Groovy DSL config"() {
    given:
        def ruleFile = TestRulesGenerator.generate(100)
    when:
        def load = new AclRulesLoader(ruleFile.toString()).load()
    then:
        load.get(SUBSCRIBE).size() == 50
        load.get(PUBLISH).size() == 50
        ruleFile.delete()
  }

  def "should throw exception if config not exists"() {
    given:
        def notExistedPath = "not/existed/path"
    when:
        new AclRulesLoader(notExistedPath)
    then:
        def exception = thrown(AclConfigurationException)
        exception.message == 'Class loader unable to load resource: not/existed/path'
    when:
        new AclRulesLoader(null)
    then:
        exception = thrown(NullPointerException)
        exception.message == null
  }

  def "should throw exception if config is invalid"(String invalidAclFileName, String errorMessage, Class<? extends Exception> exceptionClass) {
    given:
        def invalidAclPath = getAbsolutePath("acl/config/invalid/${invalidAclFileName}")
        def rules = new AclRulesLoader(invalidAclPath)
    when:
        rules.load()
    then:
        def exception = thrown CompletionException
        exceptionClass.isInstance exception.cause
        exception.cause.message.startsWith errorMessage
    where:
        invalidAclFileName | exceptionClass            | errorMessage
        "1.groovy"         | AclConfigurationException | 'Only one clients section allowed'
        "2.groovy"         | AclConfigurationException | 'Only one clients section allowed'
        "3.groovy"         | AclConfigurationException | 'AllOf condition can only have a single-matcher members'
        "4.groovy"         | MissingMethodException    | 'No signature of method: javasabr.mqtt.service.acl.builder.AllOfBuilder.allOf'
  }

  def getAbsolutePath(String fileName) {
    Objects.requireNonNull(getClass().getClassLoader().getResource(fileName)).getFile()
  }

  @SuppressWarnings('GroovyAccessibility')
  def "should parse new Groovy DSL config"() {
    when:
        def absolutePath = getAbsolutePath("acl/config/acl.groovy")
        def rules = new AclRulesLoader(absolutePath).load()
    then:
        verifyAll(rules.get(PUBLISH)) {
          size() == 2
          with(get(0)) {
            operation() == PUBLISH
            action() == ALLOW
            with(clientsAndTopics() as AllOfCondition) {
              with(conditions as Array<Condition>) {
                with(get(0) as AnyOfCondition) {
                  with(conditions as Array<Condition>) {
                    with(get(0) as UserNameCondition) {
                      with(clientMatcher as EqualsMatcher) { expectedValue == "sensor1" }
                    }
                    with(get(1) as UserNameCondition) {
                      with(clientMatcher as RegexMatcher) { pattern.pattern() == "sensor10\$" }
                    }
                    with(get(2) as ClientIdCondition) {
                      with(clientMatcher as EqualsMatcher) { expectedValue == "clientId1" }
                    }
                    with(get(3) as ClientIdCondition) {
                      with(clientMatcher as RegexMatcher) { pattern.pattern() == "^cliend" }
                    }
                    with(get(4) as IpAddressCondition) {
                      with(clientMatcher as EqualsMatcher) { expectedValue == "10.56.0.3" }
                    }
                    with(get(5) as IpAddressCondition) {
                      with(clientMatcher as EqualsMatcher) { expectedValue == "127.0.0.1" }
                    }
                    with(get(6) as AllOfCondition) {
                      with(conditions as Array<Condition>) {
                        with(get(0) as UserNameCondition) {
                          with(clientMatcher as EqualsMatcher) { expectedValue == "sensor2" }
                        }
                        with(get(1) as ClientIdCondition) {
                          with(clientMatcher as EqualsMatcher) { expectedValue == "clientId2" }
                        }
                        with(get(2) as IpAddressCondition) {
                          with(clientMatcher as EqualsMatcher) { expectedValue == "10.56.0.3" }
                        }
                      }
                    }
                  }
                }
                with(get(1) as TopicCondition) {
                  with(topics as Array) {
                    with(get(0) as EqualsMatcher) { expectedValue == "/topic1" }
                    with(get(1) as EqualsMatcher) { expectedValue == "/topic2/temp" }
                  }
                }
              }
            }
          }
        }
        verifyAll(rules.get(SUBSCRIBE)) {
          with(get(0)) {
            operation() == SUBSCRIBE
            action() == DENY
            with(clientsAndTopics() as AllOfCondition) {
              with(conditions as Array<Condition>) {
                with(get(0) as AllOfCondition) {
                  with(conditions as Array<Condition>) {
                    with(get(0) as UserNameCondition) {
                      with(clientMatcher as EqualsMatcher) { expectedValue == "sensor2" }
                    }
                    with(get(1) as ClientIdCondition) {
                      with(clientMatcher as EqualsMatcher) { expectedValue == "clientId2" }
                    }
                    with(get(2) as IpAddressCondition) {
                      with(clientMatcher as EqualsMatcher) { expectedValue == "10.56.0.3" }
                    }
                  }
                }
                with(get(1) as TopicCondition) {
                  with(topics as Array) {
                    with(get(0) as TopicFilterMatcher) { expectedValue.rawTopic == "/topic1/#" }
                    with(get(1) as TopicFilterMatcher) { expectedValue.rawTopic == "/topic2/+/temp" }
                  }
                }
              }
            }
          }
        }
  }
}
