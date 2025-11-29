package javasabr.mqtt.model.acl.condition;

import javasabr.mqtt.model.MqttUser;
import javasabr.rlib.collections.array.Array;

public record AllOfCondition(Array<MqttUserCondition> conditions) implements MqttUserCondition {

  public AllOfCondition(MqttUserCondition... conditions) {
    this(Array.of(conditions));
  }

  @Override
  public boolean test(MqttUser value) {
    for (MqttUserCondition condition : conditions) {
      if (!condition.test(value)) {
        return false;
      }
    }
    return true;
  }
}
