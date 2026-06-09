package javasabr.mqtt.acl.mug.dsl.builder;

import java.util.function.Consumer;
import javasabr.mqtt.acl.engine.model.condition.AnyOfCondition;
import javasabr.mqtt.acl.engine.model.condition.MqttUserCondition;
import javasabr.rlib.collections.array.Array;

/**
 * The builder of users sections.
 */
public class UsersBuilder extends MultiUserConditionBuilder<UsersBuilder> {

  public UsersBuilder allOf(Consumer<AllOfUserConditionBuilder> config) {
    checkAnyUser();
    AllOfUserConditionBuilder builder = new AllOfUserConditionBuilder();
    config.accept(builder);
    conditions.add(builder.build());
    return this;
  }

  public UsersBuilder anyOf(Consumer<AnyOfUserConditionBuilder> config) {
    checkAnyUser();
    AnyOfUserConditionBuilder builder = new AnyOfUserConditionBuilder();
    config.accept(builder);
    conditions.add(builder.build());
    return this;
  }

  public UsersBuilder anyUser() {
    checkAnyUser();
    conditions.add(MqttUserCondition.MATCH_ANY);
    return this;
  }

  @Override
  public MqttUserCondition build() {
    if (conditions.isEmpty()) {
      return MqttUserCondition.MATCH_NONE;
    } else if (conditions.size() > 1) {
      return new AnyOfCondition(Array.copyOf(conditions));
    } else {
      return conditions.first();
    }
  }
}
