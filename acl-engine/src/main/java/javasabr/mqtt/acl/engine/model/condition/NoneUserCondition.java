package javasabr.mqtt.acl.engine.model.condition;

import javasabr.mqtt.model.MqttUser;

public record NoneUserCondition() implements MqttUserCondition {

  @Override
  public boolean test(MqttUser requestedUser) {
    return false;
  }
}
