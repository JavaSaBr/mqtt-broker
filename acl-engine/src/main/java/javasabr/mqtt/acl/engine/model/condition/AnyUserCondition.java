package javasabr.mqtt.acl.engine.model.condition;

import com.fasterxml.jackson.annotation.JsonValue;
import javasabr.mqtt.model.MqttUser;

public record AnyUserCondition() implements MqttUserCondition {

  @Override
  public boolean test(MqttUser requestedUser) {
    return true;
  }

  @JsonValue
  Object jsonDebugValue() {
    return "AnyUser";
  }
}
