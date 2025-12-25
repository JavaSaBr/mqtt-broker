package javasabr.mqtt.acl.engine.model.condition;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Map;
import javasabr.mqtt.model.MqttUser;
import javasabr.rlib.collections.array.Array;

public record AllOfCondition(Array<MqttUserCondition> conditions) implements MqttUserCondition {

  public AllOfCondition(MqttUserCondition... conditions) {
    this(Array.of(conditions));
  }

  @Override
  public boolean test(MqttUser user) {
    for (MqttUserCondition condition : conditions) {
      if (!condition.test(user)) {
        return false;
      }
    }
    return true;
  }

  @JsonValue
  Object jsonDebugValue() {
    return Map.of("AllOf", conditions.toList());
  }
}
