package javasabr.mqtt.service.message.handler;

import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.message.in.MqttInMessage;
import javasabr.mqtt.network.user.NetworkMqttUser;

public interface MqttInMessageHandler {

  MqttMessageType messageType();

  Class<? extends NetworkMqttUser> expectedUserType();

  void processValidMessage(MqttConnection connection, MqttInMessage mqttInMessage);

  void processInvalidMessage(MqttConnection connection, MqttInMessage mqttInMessage);
}
