package javasabr.mqtt.acl.mug.dsl.builder;

import java.util.function.Consumer;
import javasabr.mqtt.acl.engine.model.condition.AnyOfCondition;
import javasabr.mqtt.acl.engine.model.condition.MqttUserCondition;
import javasabr.rlib.collections.array.Array;

public class AnyOfUserConditionBuilder extends MultiUserConditionBuilder<AnyOfUserConditionBuilder> {

  public AnyOfUserConditionBuilder allOf(Consumer<AllOfUserConditionBuilder> config) {
    conditions.add(new AllOfUserConditionBuilder()
        .apply(config)
        .build());
    return this;
  }

  public AnyOfUserConditionBuilder anyOf(Consumer<AnyOfUserConditionBuilder> config) {
    conditions.add(new AnyOfUserConditionBuilder()
        .apply(config)
        .build());
    return this;
  }

  @Override
  public MqttUserCondition build() {
    return new AnyOfCondition(Array.copyOf(conditions));
  }
}
