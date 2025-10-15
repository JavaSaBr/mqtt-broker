package javasabr.mqtt.network.packet;

import javasabr.rlib.network.Connection;
import javasabr.rlib.network.packet.NetworkPacket;

public interface HasPacketId<C extends Connection<C>> extends NetworkPacket<C> {

  int packetId();
}
