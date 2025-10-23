package javasabr.mqtt.service.message.handler;

import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.packet.MqttPacketType;
import javasabr.mqtt.network.packet.in.MqttReadablePacket;

public interface MqttInMessageHandler {

  MqttPacketType messageType();

  void processReceived(MqttConnection connection, MqttReadablePacket networkPacket);
}
