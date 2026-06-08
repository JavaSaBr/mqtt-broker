package javasabr.mqtt.acl.java.dsl.antlr;

import static javasabr.mqtt.acl.engine.model.Action.ALLOW;
import static javasabr.mqtt.acl.engine.model.Action.DENY;
import static javasabr.mqtt.model.acl.Operation.PUBLISH;
import static javasabr.mqtt.model.acl.Operation.SUBSCRIBE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
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
import javasabr.mqtt.acl.engine.model.matcher.TopicFilterMatcher;
import javasabr.mqtt.acl.engine.model.matcher.TopicNameMatcher;
import javasabr.mqtt.acl.engine.model.matcher.dynamic.DynamicTopicMatcher;
import javasabr.mqtt.acl.engine.model.rule.AbstractAclRule;
import javasabr.mqtt.acl.engine.model.rule.AclRule;
import javasabr.mqtt.acl.java.dsl.AclRulesLoader;
import javasabr.mqtt.model.acl.Operation;
import javasabr.rlib.collections.array.Array;
import org.junit.jupiter.api.Test;

class GaclParserTest {

  private static final String RESOURCES_DIR = "src/test/resources/acl";

  @Test
  @SuppressWarnings("unchecked")
  void shouldParseFullFeaturedGaclFile() {
    Path gaclFile = Paths.get(RESOURCES_DIR, "test-acl-full.gacl");
    Map<Operation, Array<AclRule>> rules = AclRulesLoader.load(gaclFile);

    Array<AclRule> publishRules = rules.get(PUBLISH);
    assertEquals(3, publishRules.size());

    // allowPublish { users { userNames { eq("sensor1") regex("sensor10$") } ... } topics { eq("/topic1") eq
    // ("/topic2/temp") } }
    AbstractAclRule rule0 = (AbstractAclRule) publishRules.get(0);
    assertEquals(PUBLISH, rule0.operation());
    assertEquals(ALLOW, rule0.action());
    assertTrue(rule0.userCondition() instanceof AnyOfCondition);
    AnyOfCondition userCond0 = (AnyOfCondition) rule0.userCondition();
    // 2 from userNames + 2 from clientIds + 2 from ipAddresses + 1 from anyOf + 1 from allOf = 8
    assertEquals(
        8,
        userCond0
            .conditions()
            .size());
    assertEquals(
        "sensor1",
        ((EqualsMatcher<String>) ((UserNameCondition) userCond0
            .conditions()
            .get(0)).matcher()).expected());
    assertEquals(
        "sensor10$",
        ((RegexMatcher) ((UserNameCondition) userCond0
            .conditions()
            .get(1)).matcher())
            .pattern()
            .pattern());
    assertEquals(
        2,
        rule0
            .topicCondition()
            .matchers()
            .size());
    assertTrue(rule0
        .topicCondition()
        .matchers()
        .get(0) instanceof TopicNameMatcher);
    assertTrue(rule0
        .topicCondition()
        .matchers()
        .get(1) instanceof TopicNameMatcher);

    // denyPublish { users { anyUser() } topics { anyTopic() } }
    AbstractAclRule rule1 = (AbstractAclRule) publishRules.get(1);
    assertEquals(PUBLISH, rule1.operation());
    assertEquals(DENY, rule1.action());
    assertEquals(MqttUserCondition.MATCH_ANY, rule1.userCondition());
    assertEquals(
        AnyTopicMatcher.instance(),
        rule1
            .topicCondition()
            .matchers()
            .get(0));

    // allowPublish { users { userName startsWith("start_with_5") clientId contains("contains") } topics { anyTopic()
    // } }
    AbstractAclRule rule2 = (AbstractAclRule) publishRules.get(2);
    assertEquals(PUBLISH, rule2.operation());
    assertEquals(ALLOW, rule2.action());
    assertTrue(rule2.userCondition() instanceof AnyOfCondition);
    AnyOfCondition userCond2 = (AnyOfCondition) rule2.userCondition();
    assertEquals(
        2,
        userCond2
            .conditions()
            .size());
    assertEquals(
        AnyTopicMatcher.instance(),
        rule2
            .topicCondition()
            .matchers()
            .get(0));

    Array<AclRule> subscribeRules = rules.get(SUBSCRIBE);
    assertEquals(4, subscribeRules.size());

    // denySubscribe { users { allOf { ... } } topics { match(...) } }
    AbstractAclRule sRule0 = (AbstractAclRule) subscribeRules.get(0);
    assertEquals(SUBSCRIBE, sRule0.operation());
    assertEquals(DENY, sRule0.action());
    assertTrue(sRule0.userCondition() instanceof AllOfCondition);

    // allowSubscribe { users { allOf { ... } } topics { match(...) } }
    AbstractAclRule sRule1 = (AbstractAclRule) subscribeRules.get(1);
    assertEquals(SUBSCRIBE, sRule1.operation());
    assertEquals(ALLOW, sRule1.action());
    assertTrue(sRule1.userCondition() instanceof AllOfCondition);

    // denySubscribe { users { anyUser() } topics { anyTopic() } }
    AbstractAclRule sRule2 = (AbstractAclRule) subscribeRules.get(2);
    assertEquals(SUBSCRIBE, sRule2.operation());
    assertEquals(DENY, sRule2.action());
    assertEquals(MqttUserCondition.MATCH_ANY, sRule2.userCondition());

    // allowSubscribe { users { clientId startsWith("device_") } topics { dynamic(...) match(...) } }
    AbstractAclRule sRule3 = (AbstractAclRule) subscribeRules.get(3);
    assertEquals(SUBSCRIBE, sRule3.operation());
    assertEquals(ALLOW, sRule3.action());
    assertTrue(sRule3.userCondition() instanceof ClientIdCondition);
    assertEquals("device_", ((StartsWithMatcher) ((ClientIdCondition) sRule3.userCondition()).matcher()).prefix());
    assertTrue(sRule3
        .topicCondition()
        .matchers()
        .get(0) instanceof DynamicTopicMatcher);
    assertTrue(sRule3
        .topicCondition()
        .matchers()
        .get(1) instanceof TopicFilterMatcher);
  }

  @Test
  void shouldParseShorthandGaclFile() {
    Path gaclFile = Paths.get(RESOURCES_DIR, "test-acl-shorthand.gacl");
    Map<Operation, Array<AclRule>> rules = AclRulesLoader.load(gaclFile);

    Array<AclRule> publishRules = rules.get(PUBLISH);
    assertEquals(3, publishRules.size());

    // allowPublish { users { clientId startsWith("device_") } topics { dynamic(...) } }
    AbstractAclRule pRule0 = (AbstractAclRule) publishRules.get(0);
    assertEquals(PUBLISH, pRule0.operation());
    assertEquals(ALLOW, pRule0.action());
    assertTrue(pRule0.userCondition() instanceof ClientIdCondition);
    assertTrue(pRule0
        .topicCondition()
        .matchers()
        .get(0) instanceof DynamicTopicMatcher);

    // allowPublish { users { clientId startsWith("service_") } topics { match(...) eq(...) } }
    AbstractAclRule pRule1 = (AbstractAclRule) publishRules.get(1);
    assertEquals(PUBLISH, pRule1.operation());
    assertEquals(ALLOW, pRule1.action());
    assertTrue(pRule1.userCondition() instanceof ClientIdCondition);
    assertEquals(
        2,
        pRule1
            .topicCondition()
            .matchers()
            .size());

    // allowPublish { users { clientId startsWith("TLS") } topics { match("tls/#") } }
    AbstractAclRule pRule2 = (AbstractAclRule) publishRules.get(2);
    assertEquals(PUBLISH, pRule2.operation());
    assertEquals(ALLOW, pRule2.action());
    assertTrue(pRule2
        .topicCondition()
        .matchers()
        .get(0) instanceof TopicFilterMatcher);

    Array<AclRule> subscribeRules = rules.get(SUBSCRIBE);
    assertEquals(4, subscribeRules.size());

    // allowSubscribe { users { clientId startsWith("system_") } topics { anyTopic() } }
    AbstractAclRule sRule2 = (AbstractAclRule) subscribeRules.get(2);
    assertEquals(SUBSCRIBE, sRule2.operation());
    assertEquals(ALLOW, sRule2.action());
    assertEquals(
        AnyTopicMatcher.instance(),
        sRule2
            .topicCondition()
            .matchers()
            .get(0));
  }

  @Test
  void shouldProduceSameResultAsAntlrDslBuilder() {
    Path gaclFile = Paths.get(RESOURCES_DIR, "test-acl-full.gacl");
    Map<Operation, Array<AclRule>> parsedRules = AclRulesLoader.load(gaclFile);

    Map<Operation, Array<AclRule>> builtRules = AclRulesLoader.build(root -> root
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

    assertEquals(
        builtRules
            .get(PUBLISH)
            .size(),
        parsedRules
            .get(PUBLISH)
            .size());
    assertEquals(
        builtRules
            .get(SUBSCRIBE)
            .size(),
        parsedRules
            .get(SUBSCRIBE)
            .size());
  }

  @Test
  void shouldThrowForNonExistentFile() {
    Path nonExistent = Paths.get("non-existent.gacl");
    AclConfigurationException exception = assertThrows(
        AclConfigurationException.class,
        () -> AclRulesLoader.load(nonExistent));
    assertTrue(exception
        .getMessage()
        .contains("doesn't exist"));
  }

  @Test
  void shouldReportSyntaxErrors() {
    Path invalidFile = Paths.get(RESOURCES_DIR, "invalid/invalid-syntax.gacl");
    AclConfigurationException exception = assertThrows(
        AclConfigurationException.class,
        () -> AclRulesLoader.load(invalidFile));
    assertTrue(exception
        .getMessage()
        .startsWith("Syntax error at line"));
  }

  @Test
  void shouldUnquoteStringsCorrectly() {
    assertEquals("simple", GaclVisitorImpl.unquote("\"simple\""));
    assertEquals("simple", GaclVisitorImpl.unquote("'simple'"));
    assertEquals("has space", GaclVisitorImpl.unquote("\"has space\""));
    assertEquals("sensor10$", GaclVisitorImpl.unquote("\"sensor10$\""));
    assertEquals("sensor10$", GaclVisitorImpl.unquote("\"sensor10\\$\""));
    assertEquals("back\\slash", GaclVisitorImpl.unquote("\"back\\\\slash\""));
    assertEquals("device/{clientId}", GaclVisitorImpl.unquote("'device/{clientId}'"));
  }
}
