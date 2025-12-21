package javasabr.mqtt.acl.engine.model.condition;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Map;
import javasabr.mqtt.model.MqttUser;
import javasabr.rlib.collections.array.Array;

public record AnyOfCondition(Array<MqttUserCondition> expectedUsers) implements MqttUserCondition {

  public AnyOfCondition(MqttUserCondition... expectedUsers) {
    this(Array.of(expectedUsers));
  }

  @Override
  public boolean test(MqttUser requestedUser) {
    for (MqttUserCondition condition : expectedUsers) {
      if (condition.test(requestedUser)) {
        return true;
      }
    }
    return false;
  }

  @JsonValue
  Object jsonDebugValue() {
    return Map.of("AnyOf", expectedUsers.toList());
  }
}
