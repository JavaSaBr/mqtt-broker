package javasabr.mqtt.service.message.handler;

import javasabr.mqtt.network.MqttClient;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.message.MqttMessageType;
import javasabr.mqtt.network.message.in.MqttInMessage;

public interface MqttInMessageHandler {

  MqttMessageType messageType();

  Class<? extends MqttClient> expectedClientType();

  void processValidMessage(MqttConnection connection, MqttInMessage mqttInMessage);

  void processInvalidMessage(MqttConnection connection, MqttInMessage mqttInMessage);
}
