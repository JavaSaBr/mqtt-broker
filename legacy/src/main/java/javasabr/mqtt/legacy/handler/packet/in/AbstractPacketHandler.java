package javasabr.mqtt.legacy.handler.packet.in;

import javasabr.mqtt.legacy.network.MqttClient.UnsafeMqttClient;
import javasabr.mqtt.legacy.network.packet.in.MqttReadablePacket;

public abstract class AbstractPacketHandler<C extends UnsafeMqttClient, R extends MqttReadablePacket> implements
    PacketInHandler {

  @Override
  public void handle(UnsafeMqttClient client, MqttReadablePacket packet) {
    //noinspection unchecked
    handleImpl((C) client, (R) packet);
  }

  protected abstract void handleImpl(C client, R packet);
}

