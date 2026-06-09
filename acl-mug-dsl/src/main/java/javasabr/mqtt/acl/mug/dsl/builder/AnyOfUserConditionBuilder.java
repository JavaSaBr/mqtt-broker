package javasabr.mqtt.acl.mug.dsl.builder;

import java.util.function.Consumer;
import javasabr.mqtt.acl.engine.model.condition.AnyOfCondition;
import javasabr.mqtt.acl.engine.model.condition.MqttUserCondition;
import javasabr.rlib.collections.array.Array;

/**
 * The builder of anyOf user condition.
 */
public class AnyOfUserConditionBuilder extends MultiUserConditionBuilder<AnyOfUserConditionBuilder> {

  public AnyOfUserConditionBuilder allOf(Consumer<AllOfUserConditionBuilder> config) {
    AllOfUserConditionBuilder builder = new AllOfUserConditionBuilder();
    config.accept(builder);
    conditions.add(builder.build());
    return this;
  }

  public AnyOfUserConditionBuilder anyOf(Consumer<AnyOfUserConditionBuilder> config) {
    AnyOfUserConditionBuilder builder = new AnyOfUserConditionBuilder();
    config.accept(builder);
    conditions.add(builder.build());
    return this;
  }

  @Override
  public MqttUserCondition build() {
    return new AnyOfCondition(Array.copyOf(conditions));
  }
}
