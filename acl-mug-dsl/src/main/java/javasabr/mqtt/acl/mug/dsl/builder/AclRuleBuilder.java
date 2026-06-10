package javasabr.mqtt.acl.mug.dsl.builder;

import java.util.function.Consumer;
import javasabr.mqtt.acl.engine.exception.AclConfigurationException;
import javasabr.mqtt.acl.engine.model.condition.MqttUserCondition;
import javasabr.mqtt.acl.engine.model.matcher.TopicMatcher;
import javasabr.mqtt.acl.engine.model.rule.AclRule;
import javasabr.rlib.collections.array.Array;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PROTECTED)
public abstract class AclRuleBuilder extends ConfigurableBuilder<AclRuleBuilder> {

  MqttUserCondition userCondition;
  Array<TopicMatcher> topicMatchers;

  public AclRuleBuilder users(Consumer<UsersBuilder> config) {
    if (userCondition != null) {
      throw new AclConfigurationException("Only one users section allowed");
    }
    userCondition = new UsersBuilder().apply(config).build();
    return this;
  }

  public AclRuleBuilder topics(Consumer<TopicsBuilder> config) {
    if (topicMatchers != null) {
      throw new AclConfigurationException("Only one topics section allowed");
    }
    topicMatchers = new TopicsBuilder().apply(config).build();
    return this;
  }

  public AclRule build() {
    if (userCondition == null) {
      throw new AclConfigurationException("Users section is not defined");
    } else if (topicMatchers == null || topicMatchers.isEmpty()) {
      throw new AclConfigurationException("Topics section is not defined");
    }
    return buildImpl();
  }

  protected abstract AclRule buildImpl();
}
