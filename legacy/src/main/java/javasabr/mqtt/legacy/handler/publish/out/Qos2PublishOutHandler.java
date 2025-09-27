package javasabr.mqtt.legacy.handler.publish.out;

import static javasabr.mqtt.legacy.model.reason.code.PublishReleaseReasonCode.SUCCESS;

import javasabr.mqtt.legacy.model.QoS;
import javasabr.mqtt.legacy.network.client.MqttClient;
import javasabr.mqtt.legacy.network.packet.HasPacketId;
import javasabr.mqtt.legacy.network.packet.in.PublishCompleteInPacket;
import javasabr.mqtt.legacy.network.packet.in.PublishReceivedInPacket;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class Qos2PublishOutHandler extends PersistentPublishOutHandler {

  @Override
  protected QoS getQoS() {
    return QoS.EXACTLY_ONCE;
  }

  @Override
  public boolean handleResponse(MqttClient client, HasPacketId response) {

    var packetOutFactory = client.getPacketOutFactory();

    if (response instanceof PublishReceivedInPacket) {
      client.send(packetOutFactory.newPublishRelease(response.getPacketId(), SUCCESS));
      return false;
    } else if (response instanceof PublishCompleteInPacket) {
      return true;
    } else {
      throw new IllegalStateException("Unexpected response: " + response);
    }
  }
}
