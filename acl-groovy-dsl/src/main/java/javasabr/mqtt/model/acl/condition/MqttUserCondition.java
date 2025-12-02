package javasabr.mqtt.model.acl.condition;

import javasabr.mqtt.model.MqttUser;

public interface MqttUserCondition {

  MqttUserCondition MATCH_ANY = new AnyUserCondition();

  boolean test(MqttUser requestedUser);
}
