package javasabr.mqtt.acl.mug.dsl

import javasabr.mqtt.acl.engine.exception.AclConfigurationException
import javasabr.mqtt.acl.engine.model.condition.AllOfCondition
import javasabr.mqtt.acl.engine.model.condition.AnyOfCondition
import javasabr.mqtt.acl.engine.model.condition.ClientIdCondition
import javasabr.mqtt.acl.engine.model.condition.MqttUserCondition
import javasabr.mqtt.acl.engine.model.condition.UserNameCondition
import javasabr.mqtt.acl.engine.model.matcher.AnyTopicMatcher
import javasabr.mqtt.acl.engine.model.matcher.EqualsMatcher
import javasabr.mqtt.acl.engine.model.matcher.RegexMatcher
import javasabr.mqtt.acl.engine.model.matcher.StartsWithMatcher
import javasabr.mqtt.acl.engine.model.matcher.TopicFilterMatcher
import javasabr.mqtt.acl.engine.model.matcher.TopicNameMatcher
import javasabr.mqtt.acl.engine.model.matcher.dynamic.DynamicTopicMatcher
import javasabr.mqtt.acl.engine.model.rule.AbstractAclRule
import javasabr.mqtt.acl.mug.dsl.loader.AclRulesLoader
import spock.lang.Specification

import java.nio.file.Paths

import static javasabr.mqtt.acl.engine.model.Action.ALLOW
import static javasabr.mqtt.acl.engine.model.Action.DENY
import static javasabr.mqtt.model.acl.Operation.PUBLISH
import static javasabr.mqtt.model.acl.Operation.SUBSCRIBE

class GaclParserTest extends Specification {

  private static final String RESOURCES_DIR = "src/test/resources/acl"

  def "should parse full featured GACL file"() {
    given:
        def gaclFile = Paths.get(RESOURCES_DIR, "test-acl-full.gacl")

    when:
        def rules = AclRulesLoader.load(gaclFile)

    then:
        with(rules.get(PUBLISH)) {
          size() == 3
          get(0) instanceof AbstractAclRule
          with(get(0) as AbstractAclRule) {
            operation() == PUBLISH
            action() == ALLOW
            userCondition() instanceof AnyOfCondition
            with(userCondition() as AnyOfCondition) {
              with(conditions()) {
                size() == 8
                get(0) instanceof UserNameCondition
                with(get(0) as UserNameCondition) {
                  matcher() instanceof EqualsMatcher
                  with(matcher() as EqualsMatcher) {
                    expected() == "sensor1"
                  }
                }
                get(1) instanceof UserNameCondition
                with(get(1) as UserNameCondition) {
                  matcher() instanceof RegexMatcher
                  with(matcher() as RegexMatcher) {
                    pattern().pattern() == "sensor10\$"
                  }
                }
              }
            }
            with(topicCondition().matchers()) {
              size() == 2
              get(0) instanceof TopicNameMatcher
              get(1) instanceof TopicNameMatcher
            }
          }
          with(get(1) as AbstractAclRule) {
            operation() == PUBLISH
            action() == DENY
            userCondition() == MqttUserCondition.MATCH_ANY
            topicCondition().matchers().get(0) == AnyTopicMatcher.instance()
          }
          with(get(2) as AbstractAclRule) {
            operation() == PUBLISH
            action() == ALLOW
            userCondition() instanceof AnyOfCondition
            with(userCondition() as AnyOfCondition) {
              conditions().size() == 2
            }
            topicCondition().matchers().get(0) == AnyTopicMatcher.instance()
          }
        }
        with(rules.get(SUBSCRIBE)) {
          size() == 4
          with(get(0) as AbstractAclRule) {
            operation() == SUBSCRIBE
            action() == DENY
            userCondition() instanceof AllOfCondition
          }
          with(get(1) as AbstractAclRule) {
            operation() == SUBSCRIBE
            action() == ALLOW
            userCondition() instanceof AllOfCondition
          }
          with(get(2) as AbstractAclRule) {
            operation() == SUBSCRIBE
            action() == DENY
            userCondition() == MqttUserCondition.MATCH_ANY
          }
          with(get(3) as AbstractAclRule) {
            operation() == SUBSCRIBE
            action() == ALLOW
            userCondition() instanceof ClientIdCondition
            with(userCondition() as ClientIdCondition) {
              matcher() instanceof StartsWithMatcher
              with(matcher() as StartsWithMatcher) {
                prefix() == "device_"
              }
            }
            with(topicCondition().matchers()) {
              get(0) instanceof DynamicTopicMatcher
              get(1) instanceof TopicFilterMatcher
            }
          }
        }
  }

  def "should parse shorthand GACL file"() {
    given:
        def gaclFile = Paths.get(RESOURCES_DIR, "test-acl-shorthand.gacl")

    when:
        def rules = AclRulesLoader.load(gaclFile)

    then:
        with(rules.get(PUBLISH)) {
          size() == 3
          with(get(0) as AbstractAclRule) {
            operation() == PUBLISH
            action() == ALLOW
            userCondition() instanceof ClientIdCondition
            topicCondition().matchers().get(0) instanceof DynamicTopicMatcher
          }
          with(get(1) as AbstractAclRule) {
            operation() == PUBLISH
            action() == ALLOW
            userCondition() instanceof ClientIdCondition
            topicCondition().matchers().size() == 2
          }
          with(get(2) as AbstractAclRule) {
            operation() == PUBLISH
            action() == ALLOW
            topicCondition().matchers().get(0) instanceof TopicFilterMatcher
          }
        }
        with(rules.get(SUBSCRIBE)) {
          size() == 4
          with(get(2) as AbstractAclRule) {
            operation() == SUBSCRIBE
            action() == ALLOW
            topicCondition().matchers().get(0) == AnyTopicMatcher.instance()
          }
        }
  }

  def "should produce same result as Mug DSL builder"() {
    given:
        def gaclFile = Paths.get(RESOURCES_DIR, "test-acl-full.gacl")
        def programmaticallyDefinedRules = { root ->
          root
              .allowPublish { rule ->
                rule
                    .users { users ->
                      users
                          .userNames { un ->
                            un
                                .eq("sensor1")
                                .regex("sensor10\$")
                          }
                          .clientIds { ci ->
                            ci
                                .eq("clientId1")
                                .regex("^cliend")
                          }
                          .ipAddresses { ip ->
                            ip
                                .eq("10.56.0.3")
                                .eq("127.0.0.1")
                          }
                          .anyOf { any -> any.userName(any.anyValue()) }
                          .allOf { all ->
                            all
                                .userName(all.eq("sensor2"))
                                .clientId(all.eq("clientId2"))
                                .ipAddress(all.eq("10.56.0.3"))
                          }
                    }
                    .topics { topics ->
                      topics
                          .eq("/topic1")
                          .eq("/topic2/temp")
                    }
              }
              .denyPublish { rule ->
                rule
                    .users { users -> users.anyUser() }
                    .topics { topics -> topics.anyTopic() }
              }
              .allowPublish { rule ->
                rule
                    .users { users ->
                      users
                          .userName(users.startsWith("start_with_5"))
                          .clientId(users.contains("contains"))
                    }
                    .topics { topics -> topics.anyTopic() }
              }
              .denySubscribe { rule ->
                rule
                    .users { users ->
                      users.allOf { all ->
                        all
                            .userName(all.eq("sensor2"))
                            .clientId(all.eq("clientId2"))
                            .ipAddress(all.eq("10.56.0.3"))
                      }
                    }
                    .topics { topics ->
                      topics
                          .match("/topic1/#")
                          .match("/topic2/+/temp")
                    }
              }
              .allowSubscribe { rule ->
                rule
                    .users { users ->
                      users.allOf { all ->
                        all
                            .userName(all.eq("sensor2"))
                            .clientId(all.eq("clientId2"))
                            .ipAddress(all.eq("10.56.0.3"))
                      }
                    }
                    .topics { topics ->
                      topics
                          .match("/topic1/#")
                          .match("/topic2/+/temp")
                    }
              }
              .denySubscribe { rule ->
                rule
                    .users { users -> users.anyUser() }
                    .topics { topics -> topics.anyTopic() }
              }
              .allowSubscribe { rule ->
                rule
                    .users { users -> users.clientId(users.startsWith("device_")) }
                    .topics { topics ->
                      topics
                          .dynamic("/devices/{clientId}/notify")
                          .match("/devices/broadcast")
                    }
              }
        }

    when:
        def parsedRules = AclRulesLoader.load(gaclFile)
        def builtRules = AclRulesLoader.build(programmaticallyDefinedRules)

    then:
        with(builtRules.get(PUBLISH)) {
          size() == parsedRules.get(PUBLISH).size()
        }
        with(builtRules.get(SUBSCRIBE)) {
          size() == parsedRules.get(SUBSCRIBE).size()
        }
  }

  def "should throw for non-existent file"() {
    given:
        def nonExistent = Paths.get("non-existent.gacl")

    when:
        AclRulesLoader.load(nonExistent)

    then:
        def e = thrown(AclConfigurationException)
        e.message.contains("doesn't exist")
  }

  def "should report syntax errors"() {
    given:
        def invalidFile = Paths.get(RESOURCES_DIR, "invalid/invalid-syntax.gacl")

    when:
        AclRulesLoader.load(invalidFile)

    then:
        def e = thrown(AclConfigurationException)
        e.message.startsWith("Syntax error at line")
  }

  def "should unquote strings correctly"() {
    expect:
        AclRulesLoader.unquote(input) == expectedOutput

    where:
        input                 | expectedOutput
        '"simple"'            | 'simple'
        "'simple'"            | 'simple'
        '"has space"'         | 'has space'
        '"sensor10$"'         | 'sensor10$'
        '"sensor10\\$"'       | 'sensor10$'
        '"back\\\\slash"'     | 'back\\slash'
        "'device/{clientId}'" | 'device/{clientId}'
  }
}
