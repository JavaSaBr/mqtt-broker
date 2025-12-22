package javasabr.mqtt.acl.engine.model.condition;

import javasabr.mqtt.model.MqttUser;

public interface MqttUserCondition {

  MqttUserCondition MATCH_ANY = new AnyUserCondition();
  MqttUserCondition MATCH_NONE = new NoneUserCondition();

  boolean test(MqttUser requestedUser);
}
