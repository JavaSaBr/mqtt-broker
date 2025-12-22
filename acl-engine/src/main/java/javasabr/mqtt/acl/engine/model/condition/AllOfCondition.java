package javasabr.mqtt.acl.engine.model.condition;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Map;
import javasabr.mqtt.model.MqttUser;
import javasabr.rlib.collections.array.Array;

public record AllOfCondition(Array<MqttUserCondition> expectedUsers) implements MqttUserCondition {

  public AllOfCondition(MqttUserCondition... expectedUsers) {
    this(Array.of(expectedUsers));
  }

  @Override
  public boolean test(MqttUser requestedUser) {
    for (MqttUserCondition condition : expectedUsers) {
      if (!condition.test(requestedUser)) {
        return false;
      }
    }
    return true;
  }

  @JsonValue
  Object jsonDebugValue() {
    return Map.of("AllOf", expectedUsers.toList());
  }
}
