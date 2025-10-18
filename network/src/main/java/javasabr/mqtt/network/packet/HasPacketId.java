package javasabr.mqtt.network.packet;

import javasabr.mqtt.network.MqttConnection;
import javasabr.rlib.network.packet.NetworkPacket;

public interface HasPacketId extends NetworkPacket<MqttConnection> {

  int packetId();
}
