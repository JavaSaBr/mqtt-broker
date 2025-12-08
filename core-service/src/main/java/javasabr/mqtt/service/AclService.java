package javasabr.mqtt.service;

import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.acl.Operation;
import javasabr.mqtt.model.topic.AbstractTopic;

public interface AclService {

  boolean authorize(MqttUser mqttUser, Operation operation, AbstractTopic topic);
}
