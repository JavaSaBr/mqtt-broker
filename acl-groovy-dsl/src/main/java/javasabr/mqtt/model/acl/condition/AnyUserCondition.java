package javasabr.mqtt.model.acl.condition;

import javasabr.mqtt.model.MqttUser;

public record AnyUserCondition() implements MqttUserCondition {

  @Override
  public boolean test(MqttUser value) {
    return true;
  }
}
