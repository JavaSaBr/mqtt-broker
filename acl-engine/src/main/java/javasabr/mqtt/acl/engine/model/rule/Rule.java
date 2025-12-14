package javasabr.mqtt.acl.engine.model.rule;

import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.acl.engine.model.Action;
import javasabr.mqtt.model.acl.Operation;
import javasabr.mqtt.model.topic.AbstractTopic;

public interface Rule {

  Operation operation();

  Action action();

  boolean test(MqttUser mqttUser, Operation operation, AbstractTopic topic);
}
