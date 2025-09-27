package com.ss.mqtt.broker.handler.packet.in;

import com.ss.mqtt.broker.network.client.MqttClient.UnsafeMqttClient;
import com.ss.mqtt.broker.network.packet.HasPacketId;
import com.ss.mqtt.broker.network.packet.in.MqttReadablePacket;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PendingOutResponseInPacketHandler<R extends MqttReadablePacket & HasPacketId> extends
    AbstractPacketHandler<UnsafeMqttClient, R> {

  @Override
  protected void handleImpl(UnsafeMqttClient client, R packet) {
    var session = client.getSession();
    if (session != null) {
      session.updateOutPendingPacket(client, packet);
    }
  }
}
