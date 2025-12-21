package javasabr.mqtt.acl.groovy.dsl.loader

import javasabr.mqtt.acl.engine.exception.AclConfigurationException
import javasabr.mqtt.acl.engine.model.condition.AllOfCondition
import javasabr.mqtt.acl.engine.model.condition.AnyOfCondition
import javasabr.mqtt.acl.engine.model.condition.ClientIdCondition
import javasabr.mqtt.acl.engine.model.condition.Condition
import javasabr.mqtt.acl.engine.model.condition.IpAddressCondition
import javasabr.mqtt.acl.engine.model.condition.MqttUserCondition
import javasabr.mqtt.acl.engine.model.condition.UserNameCondition
import javasabr.mqtt.acl.engine.model.matcher.AnyTopicMatcher
import javasabr.mqtt.acl.engine.model.matcher.ContainsMatcher
import javasabr.mqtt.acl.engine.model.matcher.EqualsMatcher
import javasabr.mqtt.acl.engine.model.matcher.RegexMatcher
import javasabr.mqtt.acl.engine.model.matcher.StartWithMatcher
import javasabr.mqtt.acl.engine.model.matcher.TopicFilterMatcher
import javasabr.mqtt.acl.engine.model.matcher.TopicNameMatcher
import javasabr.mqtt.acl.engine.model.matcher.ValueMatcher
import javasabr.mqtt.acl.engine.model.rule.AbstractAclRule
import javasabr.mqtt.service.acl.TestRulesGenerator
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
        def load = AclRulesLoader.load(ruleFile.toString())
    then:
        load.get(SUBSCRIBE).size() == 50
        load.get(PUBLISH).size() == 50
        ruleFile.delete()
  }

  def "should throw exception if config not exists"(String configPath, String errorMessage) {
    when:
        AclRulesLoader.load(configPath)
    then:
        def exception = thrown(AclConfigurationException)
        exception.message == errorMessage
    where:
        configPath         | errorMessage
        "not/existed/path" | 'Config file:[not/existed/path] doesn\'t exist'
  }

  def "should work fine with only publish rules"() {
    given:
        def onlyPublishRulesAclPath = getAbsolutePath("acl/config/acl-publish-only.gacl")
    when:
        def ruleMap = AclRulesLoader.load(onlyPublishRulesAclPath)
    then:
        noExceptionThrown()
        !ruleMap.get(PUBLISH).isEmpty()
        ruleMap.get(SUBSCRIBE).isEmpty()
  }

  def "should throw exception if config is invalid"(String invalidAclFileName, String errorMessage, Class<? extends Exception> exceptionClass) {
    given:
        def invalidAclPath = getAbsolutePath("acl/config/invalid/${invalidAclFileName}")
    when:
        AclRulesLoader.load(invalidAclPath)
    then:
        def exception = thrown CompletionException
        exceptionClass.isInstance exception.cause
        exception.cause.message.startsWith errorMessage
    where:
        invalidAclFileName | exceptionClass            | errorMessage
        "1.gacl"           | AclConfigurationException | 'Only one users section allowed'
        "2.gacl"           | AclConfigurationException | 'AllOf condition can only have single-matcher members'
        "3.gacl"           | AclConfigurationException | 'AllOf condition can only have single-matcher members'
        "4.gacl"           | MissingMethodException    | 'No signature of method: javasabr.mqtt.acl.groovy.dsl.builder.AllOfUserConditionBuilder.allOf()'
        "5.gacl"           | AclConfigurationException | 'Invalid topic name:[/topic1/#/segment3]'
        "6.gacl"           | AclConfigurationException | 'Invalid topic filter:[/topic1/#/segment3]'
        "7.gacl"           | AclConfigurationException | 'Already included any topic condition'
        "8.gacl"           | AclConfigurationException | 'Already included any user condition'
        "9.gacl"           | AclConfigurationException | 'Already included any value matcher'
  }

  def getAbsolutePath(String fileName) {
    Objects.requireNonNull(getClass().getClassLoader().getResource(fileName)).getFile()
  }

  @SuppressWarnings('GroovyAccessibility')
  def "should parse Groovy DSL config"() {
    when:
        def absolutePath = getAbsolutePath("acl/config/acl.gacl")
        def rules = AclRulesLoader.load(absolutePath)
    then:
        verifyAll(rules.get(PUBLISH)) {
          size() == 3
          with(get(0) as AbstractAclRule) {
            operation() == PUBLISH
            action() == ALLOW
            with(userCondition() as AnyOfCondition) {
              with(expectedUsers as Array<Condition>) {
                with(get(0) as UserNameCondition) {
                  with(userNameMatcher as EqualsMatcher) { expected == "sensor1" }
                }
                with(get(1) as UserNameCondition) {
                  with(userNameMatcher as RegexMatcher) { pattern.pattern() == "sensor10\$" }
                }
                with(get(2) as ClientIdCondition) {
                  with(clientIdMatcher as EqualsMatcher) { expected == "clientId1" }
                }
                with(get(3) as ClientIdCondition) {
                  with(clientIdMatcher as RegexMatcher) { pattern.pattern() == "^cliend" }
                }
                with(get(4) as IpAddressCondition) {
                  with(ipAddressMatcher as EqualsMatcher) { expected == "10.56.0.3" }
                }
                with(get(5) as IpAddressCondition) {
                  with(ipAddressMatcher as EqualsMatcher) { expected == "127.0.0.1" }
                }
                with(get(6) as AnyOfCondition) {
                  with(expectedUsers as Array<Condition>) {
                    with(get(0) as UserNameCondition) { userNameMatcher == ValueMatcher.MATCH_ANY_STRING }
                  }
                }
                with(get(7) as AllOfCondition) {
                  with(expectedUsers as Array<Condition>) {
                    with(get(0) as UserNameCondition) {
                      with(userNameMatcher as EqualsMatcher) { expected == "sensor2" }
                    }
                    with(get(1) as ClientIdCondition) {
                      with(clientIdMatcher as EqualsMatcher) { expected == "clientId2" }
                    }
                    with(get(2) as IpAddressCondition) {
                      with(ipAddressMatcher as EqualsMatcher) { expected == "10.56.0.3" }
                    }
                  }
                }
              }
            }
            with(topicCondition().matchers) {
              with(get(0) as TopicNameMatcher) { expectedTopic.rawTopic() == "/topic1" }
              with(get(1) as TopicNameMatcher) { expectedTopic.rawTopic() == "/topic2/temp" }
            }
          }
          with(get(1) as AbstractAclRule) {
            operation() == PUBLISH
            action() == DENY
            userCondition() == MqttUserCondition.MATCH_ANY
            topicCondition().matchers.get(0) == AnyTopicMatcher.instance()
          }
          with(get(2) as AbstractAclRule) {
            operation() == PUBLISH
            action() == ALLOW
            with(userCondition() as AnyOfCondition) {
              with(expectedUsers as Array<Condition>) {
                with(get(0) as UserNameCondition) {
                  with(userNameMatcher as StartWithMatcher) { prefix() == "start_with_5" }
                }
                with(get(1) as ClientIdCondition) {
                  with(clientIdMatcher as ContainsMatcher) { substring() == "contains" }
                }
              }
            }
          }
        }
        verifyAll(rules.get(SUBSCRIBE)) {
          size() == 3
          with(get(0) as AbstractAclRule) {
            operation() == SUBSCRIBE
            action() == DENY
            with(userCondition() as AllOfCondition) {
              with(expectedUsers as Array<Condition>) {
                with(get(0) as UserNameCondition) {
                  with(userNameMatcher as EqualsMatcher) { expected == "sensor2" }
                }
                with(get(1) as ClientIdCondition) {
                  with(clientIdMatcher as EqualsMatcher) { expected == "clientId2" }
                }
                with(get(2) as IpAddressCondition) {
                  with(ipAddressMatcher as EqualsMatcher) { expected == "10.56.0.3" }
                }
              }
            }
            with(topicCondition().matchers) {
              with(get(0) as TopicFilterMatcher) { expectedTopic.rawTopic == "/topic1/#" }
              with(get(1) as TopicFilterMatcher) { expectedTopic.rawTopic == "/topic2/+/temp" }
            }
          }
          with(get(1)) {
            operation() == SUBSCRIBE
            action() == ALLOW
          }
          with(get(2) as AbstractAclRule) {
            operation() == SUBSCRIBE
            action() == DENY
            userCondition() == MqttUserCondition.MATCH_ANY
            topicCondition().matchers.get(0) == AnyTopicMatcher.instance()
          }
        }
  }
}
