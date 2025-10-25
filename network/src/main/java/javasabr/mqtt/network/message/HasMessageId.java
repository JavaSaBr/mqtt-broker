package javasabr.mqtt.network.message;

import javasabr.mqtt.network.MqttConnection;
import javasabr.rlib.network.packet.NetworkPacket;

public interface HasMessageId extends NetworkPacket<MqttConnection> {

  int messageId();
}
