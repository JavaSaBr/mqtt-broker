package javasabr.mqtt.acl.engine.model.condition;

import javasabr.mqtt.model.MqttUser;

public record AnyUserCondition() implements MqttUserCondition {

  @Override
  public boolean test(MqttUser requestedUser) {
    return true;
  }
}
