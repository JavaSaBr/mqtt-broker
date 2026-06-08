package javasabr.mqtt.acl.java.dsl;

import static javasabr.mqtt.acl.engine.model.Action.ALLOW;
import static javasabr.mqtt.acl.engine.model.Action.DENY;
import static javasabr.mqtt.model.acl.Operation.PUBLISH;
import static javasabr.mqtt.model.acl.Operation.SUBSCRIBE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import javasabr.mqtt.acl.engine.exception.AclConfigurationException;
import javasabr.mqtt.acl.engine.model.condition.AllOfCondition;
import javasabr.mqtt.acl.engine.model.condition.AnyOfCondition;
import javasabr.mqtt.acl.engine.model.condition.ClientIdCondition;
import javasabr.mqtt.acl.engine.model.condition.MqttUserCondition;
import javasabr.mqtt.acl.engine.model.condition.UserNameCondition;
import javasabr.mqtt.acl.engine.model.matcher.AnyTopicMatcher;
import javasabr.mqtt.acl.engine.model.matcher.EqualsMatcher;
import javasabr.mqtt.acl.engine.model.matcher.RegexMatcher;
import javasabr.mqtt.acl.engine.model.matcher.StartsWithMatcher;
import javasabr.mqtt.acl.engine.model.matcher.dynamic.DynamicTopicMatcher;
import javasabr.mqtt.acl.engine.model.rule.AbstractAclRule;
import javasabr.mqtt.acl.engine.model.rule.AclRule;
import javasabr.mqtt.model.acl.Operation;
import javasabr.rlib.collections.array.Array;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AclRulesLoaderTest {

  @Test
  @SuppressWarnings("unchecked")
  public void shouldBuildAclRulesViaAntlrDsl() {
    Map<Operation, Array<AclRule>> rules = AclRulesLoader.build(root -> root
        .allowPublish(rule -> rule
            .users(users -> users
                .userNames(un -> un
                    .eq("sensor1")
                    .regex("sensor10$"))
                .clientIds(ci -> ci
                    .eq("clientId1")
                    .regex("^cliend"))
                .ipAddresses(ip -> ip
                    .eq("10.56.0.3")
                    .eq("127.0.0.1"))
                .anyOf(any -> any.userName(any.anyValue()))
                .allOf(all -> all
                    .userName(all.eq("sensor2"))
                    .clientId(all.eq("clientId2"))
                    .ipAddress(all.eq("10.56.0.3"))))
            .topics(topics -> topics
                .eq("/topic1")
                .eq("/topic2/temp")))
        .denyPublish(rule -> rule
            .users(users -> users.anyUser())
            .topics(topics -> topics.anyTopic()))
        .allowPublish(rule -> rule
            .users(users -> users
                .userName(users.startsWith("start_with_5"))
                .clientId(users.contains("contains")))
            .topics(topics -> topics.anyTopic()))
        .denySubscribe(rule -> rule
            .users(users -> users.allOf(all -> all
                .userName(all.eq("sensor2"))
                .clientId(all.eq("clientId2"))
                .ipAddress(all.eq("10.56.0.3"))))
            .topics(topics -> topics
                .match("/topic1/#")
                .match("/topic2/+/temp")))
        .allowSubscribe(rule -> rule
            .users(users -> users.allOf(all -> all
                .userName(all.eq("sensor2"))
                .clientId(all.eq("clientId2"))
                .ipAddress(all.eq("10.56.0.3"))))
            .topics(topics -> topics
                .match("/topic1/#")
                .match("/topic2/+/temp")))
        .denySubscribe(rule -> rule
            .users(users -> users.anyUser())
            .topics(topics -> topics.anyTopic()))
        .allowSubscribe(rule -> rule
            .users(users -> users.clientId(users.startsWith("device_")))
            .topics(topics -> topics
                .dynamic("/devices/{clientId}/notify")
                .match("/devices/broadcast"))));

    // Verification for PUBLISH rules
    Array<AclRule> publishRules = rules.get(PUBLISH);
    assertEquals(3, publishRules.size());

    AbstractAclRule rule0 = (AbstractAclRule) publishRules.get(0);
    assertEquals(PUBLISH, rule0.operation());
    assertEquals(ALLOW, rule0.action());
    assertTrue(rule0.userCondition() instanceof AnyOfCondition);
    AnyOfCondition userCond0 = (AnyOfCondition) rule0.userCondition();
    assertEquals(8,
        userCond0
            .conditions()
            .size());
    assertEquals("sensor1",
        ((EqualsMatcher<String>) ((UserNameCondition) userCond0
            .conditions()
            .get(0)).matcher()).expected());
    assertEquals("sensor10$",
        ((RegexMatcher) ((UserNameCondition) userCond0
            .conditions()
            .get(1)).matcher())
            .pattern()
            .pattern());

    AbstractAclRule rule1 = (AbstractAclRule) publishRules.get(1);
    assertEquals(PUBLISH, rule1.operation());
    assertEquals(DENY, rule1.action());
    assertEquals(MqttUserCondition.MATCH_ANY, rule1.userCondition());
    assertEquals(AnyTopicMatcher.instance(),
        rule1
            .topicCondition()
            .matchers()
            .get(0));

    AbstractAclRule rule2 = (AbstractAclRule) publishRules.get(2);
    assertEquals(PUBLISH, rule2.operation());
    assertEquals(ALLOW, rule2.action());

    // Verification for SUBSCRIBE rules
    Array<AclRule> subscribeRules = rules.get(SUBSCRIBE);
    assertEquals(4, subscribeRules.size());

    AbstractAclRule sRule0 = (AbstractAclRule) subscribeRules.get(0);
    assertEquals(SUBSCRIBE, sRule0.operation());
    assertEquals(DENY, sRule0.action());
    assertTrue(sRule0.userCondition() instanceof AllOfCondition);

    AbstractAclRule sRule3 = (AbstractAclRule) subscribeRules.get(3);
    assertEquals(SUBSCRIBE, sRule3.operation());
    assertEquals(ALLOW, sRule3.action());
    assertTrue(sRule3.userCondition() instanceof ClientIdCondition);
    assertEquals("device_", ((StartsWithMatcher) ((ClientIdCondition) sRule3.userCondition()).matcher()).prefix());

    // Skip testing internals of DynamicTopicMatcher as they are not publicly accessible
    assertTrue(sRule3
        .topicCondition()
        .matchers()
        .get(0) instanceof DynamicTopicMatcher);
  }

  @Test
  public void shouldThrowExceptionIfTopicsSectionIsMissing() {
    Assertions.assertThrows(
        AclConfigurationException.class,
        () -> AclRulesLoader.build(root -> root.allowPublish(rule -> rule.users(users -> users.anyUser()))));
  }

  @Test
  public void shouldThrowExceptionIfUsersSectionIsMissing() {
    Assertions.assertThrows(
        AclConfigurationException.class,
        () -> AclRulesLoader.build(root -> root.allowPublish(rule -> rule.topics(topics -> topics.anyTopic()))));
  }

  @Test
  public void shouldThrowExceptionIfMultipleUsersSections() {
    Assertions.assertThrows(
        AclConfigurationException.class,
        () -> AclRulesLoader.build(root -> root.allowPublish(rule -> rule
            .users(users -> users.anyUser())
            .users(users -> users.anyUser()))));
  }

  @Test
  public void shouldThrowExceptionIfMultipleTopicsSections() {
    Assertions.assertThrows(
        AclConfigurationException.class,
        () -> AclRulesLoader.build(root -> root.allowPublish(rule -> rule
            .users(users -> users.anyUser())
            .topics(topics -> topics.anyTopic())
            .topics(topics -> topics.anyTopic()))));
  }
}
