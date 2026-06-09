package javasabr.mqtt.acl.antlr.dsl.loader;

import java.util.function.Consumer;
import javasabr.mqtt.acl.engine.exception.AclConfigurationException;
import javasabr.mqtt.acl.engine.model.matcher.UserMatchers;
import javasabr.mqtt.acl.engine.model.matcher.ValueMatcher;
import javasabr.mqtt.acl.java.dsl.GaclBaseVisitor;
import javasabr.mqtt.acl.java.dsl.GaclParser.AllOfConditionContext;
import javasabr.mqtt.acl.java.dsl.GaclParser.AllowPublishDirectiveContext;
import javasabr.mqtt.acl.java.dsl.GaclParser.AllowSubscribeDirectiveContext;
import javasabr.mqtt.acl.java.dsl.GaclParser.AnyOfConditionContext;
import javasabr.mqtt.acl.java.dsl.GaclParser.AnyTopicContext;
import javasabr.mqtt.acl.java.dsl.GaclParser.AnyUserConditionContext;
import javasabr.mqtt.acl.java.dsl.GaclParser.AnyValueMatcherContext;
import javasabr.mqtt.acl.java.dsl.GaclParser.BlockUserConditionContext;
import javasabr.mqtt.acl.java.dsl.GaclParser.ContainsMatcherContext;
import javasabr.mqtt.acl.java.dsl.GaclParser.DenyPublishDirectiveContext;
import javasabr.mqtt.acl.java.dsl.GaclParser.DenySubscribeDirectiveContext;
import javasabr.mqtt.acl.java.dsl.GaclParser.DynamicTopicContext;
import javasabr.mqtt.acl.java.dsl.GaclParser.EqMatcherContext;
import javasabr.mqtt.acl.java.dsl.GaclParser.EqTopicContext;
import javasabr.mqtt.acl.java.dsl.GaclParser.MatchTopicContext;
import javasabr.mqtt.acl.java.dsl.GaclParser.UserMatcherContext;
import javasabr.mqtt.acl.java.dsl.GaclParser.RegexMatcherContext;
import javasabr.mqtt.acl.java.dsl.GaclParser.RuleBodyContext;
import javasabr.mqtt.acl.java.dsl.GaclParser.ShorthandUserConditionContext;
import javasabr.mqtt.acl.java.dsl.GaclParser.StartsWithMatcherContext;
import javasabr.mqtt.acl.java.dsl.GaclParser.TopicMatcherContext;
import javasabr.mqtt.acl.java.dsl.GaclParser.TopicsSectionContext;
import javasabr.mqtt.acl.java.dsl.GaclParser.UserConditionContext;
import javasabr.mqtt.acl.java.dsl.GaclParser.UsersSectionContext;
import javasabr.mqtt.acl.antlr.dsl.builder.AclRuleBuilder;
import javasabr.mqtt.acl.antlr.dsl.builder.AclRulesBuilder;
import javasabr.mqtt.acl.antlr.dsl.builder.AllOfUserConditionBuilder;
import javasabr.mqtt.acl.antlr.dsl.builder.AnyOfUserConditionBuilder;
import javasabr.mqtt.acl.antlr.dsl.builder.MultiUserConditionBuilder;
import javasabr.mqtt.acl.antlr.dsl.builder.TopicsBuilder;
import javasabr.mqtt.acl.antlr.dsl.builder.UserConditionBuilder;
import javasabr.mqtt.acl.antlr.dsl.builder.UserMatchersBuilder;
import javasabr.mqtt.acl.antlr.dsl.builder.UsersBuilder;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GaclVisitorImpl extends GaclBaseVisitor<Void> {

  private final AclRulesBuilder aclRulesBuilder;

  @Override
  public Void visitAllowPublishDirective(AllowPublishDirectiveContext ctx) {
    aclRulesBuilder.allowPublish(rb -> configureRule(rb, ctx.ruleBody()));
    return null;
  }

  @Override
  public Void visitDenyPublishDirective(DenyPublishDirectiveContext ctx) {
    aclRulesBuilder.denyPublish(rb -> configureRule(rb, ctx.ruleBody()));
    return null;
  }

  @Override
  public Void visitAllowSubscribeDirective(AllowSubscribeDirectiveContext ctx) {
    aclRulesBuilder.allowSubscribe(rb -> configureRule(rb, ctx.ruleBody()));
    return null;
  }

  @Override
  public Void visitDenySubscribeDirective(DenySubscribeDirectiveContext ctx) {
    aclRulesBuilder.denySubscribe(rb -> configureRule(rb, ctx.ruleBody()));
    return null;
  }

  private void configureRule(AclRuleBuilder rb, RuleBodyContext ctx) {
    rb.users(ub -> configureUsers(ub, ctx.usersSection()));
    rb.topics(tb -> configureTopics(tb, ctx.topicsSection()));
  }

  private void configureUsers(UsersBuilder ub, UsersSectionContext ctx) {
    for (UserConditionContext uc : ctx.userCondition()) {
      applyUsersCondition(ub, uc);
    }
  }

  private void applyUsersCondition(UsersBuilder builder, UserConditionContext ctx) {
    switch (ctx) {
      case AnyUserConditionContext _ -> builder.anyUser();
      case ShorthandUserConditionContext c -> applyShorthand(builder, c);
      case BlockUserConditionContext c -> applyBlock(builder, c);
      case AllOfConditionContext c -> builder.allOf(ab -> applyAllOfConditions(ab, c));
      case AnyOfConditionContext c -> builder.anyOf(ab -> applyAnyOfConditions(ab, c));
      default -> throw new AclConfigurationException("Unexpected user condition: " + ctx.getText());
    }
  }

  private void applyAllOfConditions(AllOfUserConditionBuilder builder, AllOfConditionContext ctx) {
    for (UserConditionContext uc : ctx.userCondition()) {
      switch (uc) {
        case ShorthandUserConditionContext c -> applyShorthand(builder, c);
        case AnyOfConditionContext c -> builder.anyOf(ab -> applyAnyOfConditions(ab, c));
        default -> throw new AclConfigurationException("Unsupported condition inside allOf: " + uc.getText());
      }
    }
  }

  private void applyAnyOfConditions(AnyOfUserConditionBuilder builder, AnyOfConditionContext ctx) {
    for (UserConditionContext uc : ctx.userCondition()) {
      switch (uc) {
        case ShorthandUserConditionContext c -> applyShorthand(builder, c);
        case BlockUserConditionContext c -> applyBlock(builder, c);
        case AllOfConditionContext c -> builder.allOf(ab -> applyAllOfConditions(ab, c));
        case AnyOfConditionContext c -> builder.anyOf(ab -> applyAnyOfConditions(ab, c));
        default -> throw new AclConfigurationException("Unsupported condition inside anyOf: " + uc.getText());
      }
    }
  }

  private <B extends UserConditionBuilder<B>> void applyShorthand(B builder, ShorthandUserConditionContext ctx) {
    ValueMatcher<String> matcher = resolveMatcher(ctx.userMatcher());
    String identity = ctx
        .identityType()
        .getText();
    switch (identity) {
      case "userName" -> builder.userName(matcher);
      case "clientId" -> builder.clientId(matcher);
      case "ipAddress" -> builder.ipAddress(matcher);
      default -> throw new AclConfigurationException("Unknown identity type: " + identity);
    }
  }

  private <B extends MultiUserConditionBuilder<B>> void applyBlock(B builder, BlockUserConditionContext ctx) {
    String identity = ctx
        .identityBlockType()
        .getText();
    Consumer<UserMatchersBuilder> config = mb -> {
      for (UserMatcherContext mc : ctx.userMatcher()) {
        applyMatcherCall(mb, mc);
      }
    };
    switch (identity) {
      case "userNames" -> builder.userNames(config);
      case "clientIds" -> builder.clientIds(config);
      case "ipAddresses" -> builder.ipAddresses(config);
      default -> throw new AclConfigurationException("Unknown identity block type: " + identity);
    }
  }

  private void applyMatcherCall(UserMatchersBuilder mb, UserMatcherContext ctx) {
    switch (ctx) {
      case StartsWithMatcherContext c -> mb.startsWith(unquote(c
          .STRING()
          .getText()));
      case ContainsMatcherContext c -> mb.contains(unquote(c
          .STRING()
          .getText()));
      case EqMatcherContext c -> mb.eq(unquote(c
          .STRING()
          .getText()));
      case RegexMatcherContext c -> mb.regex(unquote(c
          .STRING()
          .getText()));
      case AnyValueMatcherContext c -> mb.anyValue();
      default -> throw new AclConfigurationException("Unknown matcher: " + ctx.getText());
    }
  }

  private ValueMatcher<String> resolveMatcher(UserMatcherContext ctx) {
    switch (ctx) {
      case StartsWithMatcherContext c -> {
        return UserMatchers.startsWith(unquote(c
            .STRING()
            .getText()));
      }
      case ContainsMatcherContext c -> {
        return UserMatchers.contains(unquote(c
            .STRING()
            .getText()));
      }
      case EqMatcherContext c -> {
        return UserMatchers.eq(unquote(c
            .STRING()
            .getText()));
      }
      case RegexMatcherContext c -> {
        return UserMatchers.regex(unquote(c
            .STRING()
            .getText()));
      }
      case AnyValueMatcherContext c -> {
        return ValueMatcher.MATCH_ANY_STRING;
      }
      default -> throw new AclConfigurationException("Unknown matcher: " + ctx.getText());
    }
  }

  private void configureTopics(TopicsBuilder tb, TopicsSectionContext ctx) {
    for (TopicMatcherContext tc : ctx.topicMatcher()) {
      switch (tc) {
        case EqTopicContext c -> tb.eq(unquote(c
            .STRING()
            .getText()));
        case MatchTopicContext c -> tb.match(unquote(c
            .STRING()
            .getText()));
        case DynamicTopicContext c -> tb.dynamic(unquote(c
            .STRING()
            .getText()));
        case AnyTopicContext c -> tb.anyTopic();
        default -> throw new AclConfigurationException("Unknown topic matcher: " + tc.getText());
      }
    }
  }

  static String unquote(String text) {
    String content = text.substring(1, text.length() - 1);
    StringBuilder sb = new StringBuilder(content.length());
    for (int i = 0; i < content.length(); i++) {
      char c = content.charAt(i);
      if (c == '\\' && i + 1 < content.length()) {
        sb.append(content.charAt(i + 1));
        i++;
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }
}
