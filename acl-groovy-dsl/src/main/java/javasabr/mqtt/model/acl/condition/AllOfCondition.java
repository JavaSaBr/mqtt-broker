package javasabr.mqtt.model.acl.condition;

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
}
