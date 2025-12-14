package javasabr.mqtt.service.acl

import javasabr.mqtt.acl.engine.exception.AclConfigurationException
import javasabr.mqtt.acl.engine.model.Action
import javasabr.mqtt.acl.engine.model.condition.AllOfCondition
import javasabr.mqtt.acl.engine.model.condition.AnyOfCondition
import javasabr.mqtt.acl.engine.model.condition.ClientIdCondition
import javasabr.mqtt.acl.engine.model.condition.Condition
import javasabr.mqtt.acl.engine.model.condition.IpAddressCondition
import javasabr.mqtt.acl.engine.model.condition.MqttUserCondition
import javasabr.mqtt.acl.engine.model.condition.UserNameCondition
import javasabr.mqtt.acl.engine.model.matcher.EqualsMatcher
import javasabr.mqtt.acl.engine.model.matcher.RegexMatcher
import javasabr.mqtt.acl.engine.model.matcher.TopicFilterMatcher
import javasabr.mqtt.acl.engine.model.matcher.TopicNameMatcher
import javasabr.mqtt.acl.engine.model.matcher.ValueMatcher
import javasabr.mqtt.acl.engine.model.rule.AbstractRule
import javasabr.mqtt.model.acl.Operation
import javasabr.mqtt.test.support.UnitSpecification
import javasabr.rlib.collections.array.Array

import java.util.concurrent.CompletionException

import static javasabr.mqtt.acl.engine.model.Action.ALLOW
import static javasabr.mqtt.acl.engine.model.Action.DENY
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

  def "should throw exception if config not exists"(String configPath, String errorMessage) {
    when:
        new AclRulesLoader(configPath)
    then:
        def exception = thrown(AclConfigurationException)
        exception.message == errorMessage
    where:
        configPath         | errorMessage
        "not/existed/path" | 'Class loader unable to load resource: not/existed/path'
        null               | 'ACL config path is null'
  }

  def "should work fine with only publish rules"() {
    given:
        def onlyPublishRulesAclPath = getAbsolutePath("acl/config/acl-publish-only.groovy")
        def rules = new AclRulesLoader(onlyPublishRulesAclPath)
    when:
        def ruleMap = rules.load()
    then:
        noExceptionThrown()
        !ruleMap.get(PUBLISH).isEmpty()
        ruleMap.get(SUBSCRIBE).isEmpty()
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
        "1.config"         | AclConfigurationException | 'Only one clients section allowed'
        "2.config"         | AclConfigurationException | 'Only one clients section allowed'
        "3.config"         | AclConfigurationException | 'AllOf condition can only have single-matcher members'
        "4.config"         | MissingMethodException    | 'No signature of method: javasabr.mqtt.service.acl.builder.AllOfBuilder.allOf'
        "5.config"         | AclConfigurationException | 'AllOf condition can only have single-matcher members'
  }

  def getAbsolutePath(String fileName) {
    Objects.requireNonNull(getClass().getClassLoader().getResource(fileName)).getFile()
  }

  @SuppressWarnings('GroovyAccessibility')
  def "should parse Groovy DSL config"() {
    when:
        def absolutePath = getAbsolutePath("acl/config/acl.groovy")
        def rules = new AclRulesLoader(absolutePath).load()
    then:
        verifyAll(rules.get(PUBLISH)) {
          size() == 2
          with(get(0) as AbstractRule) {
            operation() == PUBLISH
            action() == ALLOW
            with(userCondition() as AnyOfCondition) {
              with(expectedUsers as Array<Condition>) {
                with(get(0) as UserNameCondition) {
                  with(userNameMatcher as EqualsMatcher) { expectedValue == "sensor1" }
                }
                with(get(1) as UserNameCondition) {
                  with(userNameMatcher as RegexMatcher) { pattern.pattern() == "sensor10\$" }
                }
                with(get(2) as ClientIdCondition) {
                  with(expectedClientId as EqualsMatcher) { expectedValue == "clientId1" }
                }
                with(get(3) as ClientIdCondition) {
                  with(expectedClientId as RegexMatcher) { pattern.pattern() == "^cliend" }
                }
                with(get(4) as IpAddressCondition) {
                  with(expectedIpAddress as EqualsMatcher) { expectedValue == "10.56.0.3" }
                }
                with(get(5) as IpAddressCondition) {
                  with(expectedIpAddress as EqualsMatcher) { expectedValue == "127.0.0.1" }
                }
                with(get(6) as AnyOfCondition) {
                  with(expectedUsers as Array<Condition>) {
                    with(get(0) as UserNameCondition) { userNameMatcher == ValueMatcher.MATCH_ANY }
                  }
                }
                with(get(7) as AllOfCondition) {
                  with(expectedUsers as Array<Condition>) {
                    with(get(0) as UserNameCondition) {
                      with(userNameMatcher as EqualsMatcher) { expectedValue == "sensor2" }
                    }
                    with(get(1) as ClientIdCondition) {
                      with(expectedClientId as EqualsMatcher) { expectedValue == "clientId2" }
                    }
                    with(get(2) as IpAddressCondition) {
                      with(expectedIpAddress as EqualsMatcher) { expectedValue == "10.56.0.3" }
                    }
                  }
                }
              }
            }
            with(topicCondition().expectedTopics) {
              with(get(0) as TopicNameMatcher) { expectedTopic.rawTopic() == "/topic1" }
              with(get(1) as TopicNameMatcher) { expectedTopic.rawTopic() == "/topic2/temp" }
            }
          }
          with(get(1) as AbstractRule) {
            operation() == PUBLISH
            action() == DENY
            userCondition() == MqttUserCondition.MATCH_ANY
            topicCondition().expectedTopics.get(0) == ValueMatcher.MATCH_ANY
          }
        }
        verifyAll(rules.get(SUBSCRIBE)) {
          size() == 3
          with(get(0) as AbstractRule) {
            operation() == SUBSCRIBE
            action() == DENY
            with(userCondition() as AllOfCondition) {
              with(expectedUsers as Array<Condition>) {
                with(get(0) as UserNameCondition) {
                  with(userNameMatcher as EqualsMatcher) { expectedValue == "sensor2" }
                }
                with(get(1) as ClientIdCondition) {
                  with(expectedClientId as EqualsMatcher) { expectedValue == "clientId2" }
                }
                with(get(2) as IpAddressCondition) {
                  with(expectedIpAddress as EqualsMatcher) { expectedValue == "10.56.0.3" }
                }
              }
            }
            with(topicCondition().expectedTopics) {
              with(get(0) as TopicFilterMatcher) { expectedTopic.rawTopic == "/topic1/#" }
              with(get(1) as TopicFilterMatcher) { expectedTopic.rawTopic == "/topic2/+/temp" }
            }
          }
          with(get(1)) {
            operation() == SUBSCRIBE
            action() == ALLOW
          }
          with(get(2) as AbstractRule) {
            operation() == SUBSCRIBE
            action() == DENY
            userCondition() == MqttUserCondition.MATCH_ANY
            topicCondition().expectedTopics.get(0) == ValueMatcher.MATCH_ANY
          }
        }
  }
}
