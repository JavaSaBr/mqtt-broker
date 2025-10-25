package javasabr.mqtt.service.message.handler;

import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.message.MqttMessageType;
import javasabr.mqtt.network.message.in.MqttInMessage;

public interface MqttInMessageHandler {

  MqttMessageType messageType();

  void processReceived(MqttConnection connection, MqttInMessage networkPacket);
}
