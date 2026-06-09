package javasabr.mqtt.acl.mug.dsl.loader

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
import javasabr.mqtt.acl.engine.model.matcher.dynamic.DynamicTopicMatcher
import javasabr.mqtt.acl.engine.model.rule.AbstractAclRule
import spock.lang.Specification

import static javasabr.mqtt.acl.engine.model.Action.ALLOW
import static javasabr.mqtt.acl.engine.model.Action.DENY
import static javasabr.mqtt.model.acl.Operation.PUBLISH
import static javasabr.mqtt.model.acl.Operation.SUBSCRIBE

class AclRulesLoaderTest extends Specification {

  def "should build ACL rules via Mug DSL"() {
    given: "a complex ACL rule configuration"
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

    when: "the rules are built"
        def rules = AclRulesLoader.build(programmaticallyDefinedRules)

    then: "the rules are correct for PUBLISH"
        def publishRules = rules.get(PUBLISH)
        publishRules.size() == 3

        def rule0 = (AbstractAclRule) publishRules.get(0)
        rule0.operation() == PUBLISH
        rule0.action() == ALLOW
        rule0.userCondition() instanceof AnyOfCondition
        def userCond0 = (AnyOfCondition) rule0.userCondition()
        userCond0.conditions().size() == 8
        ((EqualsMatcher<String>) ((UserNameCondition) userCond0.conditions().get(0)).matcher()).expected() == "sensor1"
        ((RegexMatcher) ((UserNameCondition) userCond0.conditions().get(1)).matcher()).pattern().pattern() == "sensor10\$"

        def rule1 = (AbstractAclRule) publishRules.get(1)
        rule1.operation() == PUBLISH
        rule1.action() == DENY
        rule1.userCondition() == MqttUserCondition.MATCH_ANY
        rule1.topicCondition().matchers().get(0) == AnyTopicMatcher.instance()

        def rule2 = (AbstractAclRule) publishRules.get(2)
        rule2.operation() == PUBLISH
        rule2.action() == ALLOW

    and: "the rules are correct for SUBSCRIBE"
        def subscribeRules = rules.get(SUBSCRIBE)
        subscribeRules.size() == 4

        def sRule0 = (AbstractAclRule) subscribeRules.get(0)
        sRule0.operation() == SUBSCRIBE
        sRule0.action() == DENY
        sRule0.userCondition() instanceof AllOfCondition

        def sRule3 = (AbstractAclRule) subscribeRules.get(3)
        sRule3.operation() == SUBSCRIBE
        sRule3.action() == ALLOW
        sRule3.userCondition() instanceof ClientIdCondition
        ((StartsWithMatcher) ((ClientIdCondition) sRule3.userCondition()).matcher()).prefix() == "device_"
        sRule3.topicCondition().matchers().get(0) instanceof DynamicTopicMatcher
  }

  def "should throw exception if topics section is missing"() {
    when:
        AclRulesLoader.build { root -> root.allowPublish { rule -> rule.users { users -> users.anyUser() } } }

    then:
        thrown(AclConfigurationException)
  }

  def "should throw exception if users section is missing"() {
    when:
        AclRulesLoader.build { root -> root.allowPublish { rule -> rule.topics { topics -> topics.anyTopic() } } }

    then:
        thrown(AclConfigurationException)
  }

  def "should throw exception if multiple users sections"() {
    when:
        AclRulesLoader.build { root ->
          root.allowPublish { rule ->
            rule
                .users { users -> users.anyUser() }
                .users { users -> users.anyUser() }
          }
        }

    then:
        thrown(AclConfigurationException)
  }

  def "should throw exception if multiple topics sections"() {
    when:
        AclRulesLoader.build { root ->
          root.allowPublish { rule ->
            rule
                .users { users -> users.anyUser() }
                .topics { topics -> topics.anyTopic() }
                .topics { topics -> topics.anyTopic() }
          }
        }

    then:
        thrown(AclConfigurationException)
  }
}
