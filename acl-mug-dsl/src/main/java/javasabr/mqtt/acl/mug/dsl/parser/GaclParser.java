package javasabr.mqtt.acl.mug.dsl.parser;

import com.google.common.labs.parse.Parser;
import com.google.mu.util.CharPredicate;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import javasabr.mqtt.acl.engine.exception.AclConfigurationException;
import javasabr.mqtt.acl.engine.model.condition.AllOfCondition;
import javasabr.mqtt.acl.engine.model.condition.AnyOfCondition;
import javasabr.mqtt.acl.engine.model.condition.ClientIdCondition;
import javasabr.mqtt.acl.engine.model.condition.IpAddressCondition;
import javasabr.mqtt.acl.engine.model.condition.MqttUserCondition;
import javasabr.mqtt.acl.engine.model.condition.TopicCondition;
import javasabr.mqtt.acl.engine.model.condition.UserNameCondition;
import javasabr.mqtt.acl.engine.model.matcher.TopicFilterMatcher;
import javasabr.mqtt.acl.engine.model.matcher.TopicMatcher;
import javasabr.mqtt.acl.engine.model.matcher.TopicNameMatcher;
import javasabr.mqtt.acl.engine.model.matcher.UserMatchers;
import javasabr.mqtt.acl.engine.model.matcher.ValueMatcher;
import javasabr.mqtt.acl.engine.model.matcher.dynamic.DynamicTopicMatcher;
import javasabr.mqtt.acl.engine.model.rule.AclRule;
import javasabr.mqtt.acl.engine.model.rule.AllowPublishAclRule;
import javasabr.mqtt.acl.engine.model.rule.AllowSubscribeAclRule;
import javasabr.mqtt.acl.engine.model.rule.DenyPublishAclRule;
import javasabr.mqtt.acl.engine.model.rule.DenySubscribeAclRule;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.mqtt.model.topic.TopicValidator;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.collections.array.ArrayBuilder;
import javasabr.rlib.collections.array.ArrayCollectors;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GaclParser {

  CharPredicate whitespace = CharPredicate.anyOf(" \t\r\n");

  Parser<String> string = Parser.anyOf(
      Parser.quotedByWithEscapes('"', '"', Parser.chars(1)),
      Parser.quotedByWithEscapes('\'', '\'', Parser.chars(1)));

  Parser<ValueMatcher<String>> userMatcher = Parser.anyOf(
      Parser
          .word("startsWith")
          .then(string.between("(", ")"))
          .map(UserMatchers::startsWith),
      Parser
          .word("contains")
          .then(string.between("(", ")"))
          .map(UserMatchers::contains),
      Parser
          .word("eq")
          .then(string.between("(", ")"))
          .map(UserMatchers::eq),
      Parser
          .word("regex")
          .then(string.between("(", ")"))
          .map(UserMatchers::regex),
      Parser
          .word("anyValue")
          .followedBy("()")
          .thenReturn(ValueMatcher.MATCH_ANY_STRING));

  Parser<String> identityType = Parser.anyOf(
      Parser.word("userName"),
      Parser.word("clientId"),
      Parser.word("ipAddress"));

  Parser<String> identityBlockType = Parser.anyOf(
      Parser.word("userNames"),
      Parser.word("clientIds"),
      Parser.word("ipAddresses"));

  Parser<List<MqttUserCondition>> userCondition = Parser.define(uc -> Parser.<List<MqttUserCondition>>anyOf(
      Parser
          .word("anyUser")
          .followedBy("()")
          .thenReturn(List.of(MqttUserCondition.MATCH_ANY)),
      Parser.sequence(identityType, userMatcher, (id, matcher) -> List.of(toUserCondition(id, matcher))),
      Parser.sequence(
          identityBlockType,
          userMatcher
              .atLeastOnce()
              .between("{", "}"),
          (id, matchers) -> matchers
              .stream()
              .map(matcher -> toUserCondition(id, matcher))
              .collect(Collectors.toList())),
      Parser
          .word("allOf")
          .then(uc
              .atLeastOnce()
              .between("{", "}")
              .map(lists -> {
                Array<MqttUserCondition> flat = lists
                    .stream()
                    .flatMap(List::stream)
                    .collect(ArrayCollectors.toArray(MqttUserCondition.class));
                return List.of(new AllOfCondition(flat));
              })),
      Parser
          .word("anyOf")
          .then(uc
              .atLeastOnce()
              .between("{", "}")
              .map(lists -> {
                Array<MqttUserCondition> flat = lists
                    .stream()
                    .flatMap(List::stream)
                    .collect(ArrayCollectors.toArray(MqttUserCondition.class));
                return List.of(new AnyOfCondition(flat));
              }))));

  Parser<MqttUserCondition> usersSection = Parser
      .word("users")
      .then(userCondition
          .atLeastOnce()
          .between("{", "}")
          .map(lists -> {
            Array<MqttUserCondition> flat = lists
                .stream()
                .flatMap(List::stream)
                .collect(ArrayCollectors.toArray(MqttUserCondition.class));
            if (flat.isEmpty()) {
              return MqttUserCondition.MATCH_NONE;
            }
            if (flat.size() == 1) {
              return flat.get(0);
            }
            return new AnyOfCondition(flat);
          }));

  Parser<TopicMatcher> topicMatcher = Parser.anyOf(
      Parser
          .word("eq")
          .then(string.between("(", ")"))
          .map(s -> {
            if (!TopicValidator.validateTopicName(s)) {
              throw new AclConfigurationException("Invalid topic name:[%s]".formatted(s));
            }
            return (TopicMatcher) new TopicNameMatcher(TopicName.valueOf(s));
          }),
      Parser
          .word("match")
          .then(string.between("(", ")"))
          .map(s -> {
            if (!TopicValidator.validateTopicFilter(s)) {
              throw new AclConfigurationException("Invalid topic filter:[%s]".formatted(s));
            }
            return (TopicMatcher) new TopicFilterMatcher(TopicFilter.valueOf(s));
          }),
      Parser
          .word("dynamic")
          .then(string.between("(", ")"))
          .map(s -> {
            try {
              return DynamicTopicMatcher.autoBuild(s);
            } catch (RuntimeException e) {
              throw new AclConfigurationException("Invalid dynamic topic pattern", e);
            }
          }),
      Parser
          .word("anyTopic")
          .followedBy("()")
          .thenReturn(TopicMatcher.MATCH_ANY));

  Parser<Array<TopicMatcher>> topicsSection = Parser
      .word("topics")
      .then(topicMatcher
          .atLeastOnce()
          .between("{", "}")
          .map(matchers -> new ArrayBuilder<>(TopicMatcher.class).add(matchers))
          .map(ArrayBuilder::build));

  Parser<AclRule> directive = Parser.anyOf(
      createDirective("allowPublish", AllowPublishAclRule::new),
      createDirective("denyPublish", DenyPublishAclRule::new),
      createDirective("allowSubscribe", AllowSubscribeAclRule::new),
      createDirective("denySubscribe", DenySubscribeAclRule::new));

  public List<AclRule> parse(String input) {
    try {
      return directive
          .atLeastOnce()
          .parseSkipping(whitespace, input);
    } catch (Parser.ParseException e) {
      throw new AclConfigurationException("Syntax error at %s".formatted(toLineColumn(input, e.getSourceIndex())));
    }
  }

  private Parser<AclRule> createDirective(
      String keyword,
      BiFunction<MqttUserCondition, TopicCondition, AclRule> factory) {
    return Parser
        .word(keyword)
        .then(Parser
            .sequence(usersSection, topicsSection, (uc, tm) -> factory.apply(uc, new TopicCondition(tm)))
            .between("{", "}"));
  }

  private MqttUserCondition toUserCondition(String identityType, ValueMatcher<String> matcher) {
    return switch (identityType) {
      case "userName", "userNames" -> new UserNameCondition(matcher);
      case "clientId", "clientIds" -> new ClientIdCondition(matcher);
      case "ipAddress", "ipAddresses" -> new IpAddressCondition(matcher);
      default -> throw new AclConfigurationException("Unknown identity type: %s".formatted(identityType));
    };
  }

  private String toLineColumn(String input, int index) {
    int line = 1;
    int col = 1;
    for (int i = 0; i < index && i < input.length(); i++) {
      if (input.charAt(i) == '\n') {
        line++;
        col = 1;
      } else {
        col++;
      }
    }
    return "line %s, column %s".formatted(line, col);
  }
}
