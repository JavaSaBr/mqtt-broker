package javasabr.mqtt.network.packet;

import javasabr.rlib.network.packet.Packet;

public interface HasPacketId extends Packet {

  int getPacketId();
}
