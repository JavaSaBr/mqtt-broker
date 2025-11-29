package javasabr.mqtt.model.acl.condition;

import javasabr.mqtt.model.MqttUser;

public interface MqttUserCondition {

  MqttUserCondition MATCH_ANY = new AnyCondition();

  boolean test(MqttUser value);
}
