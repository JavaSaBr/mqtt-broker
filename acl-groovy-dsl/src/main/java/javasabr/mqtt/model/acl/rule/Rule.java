package javasabr.mqtt.model.acl.rule;

import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.acl.Action;
import javasabr.mqtt.model.acl.Operation;
import javasabr.mqtt.model.topic.AbstractTopic;

public interface Rule {

  Operation operation();

  Action action();

  boolean test(MqttUser mqttUser, Operation operation, AbstractTopic topic);
}
