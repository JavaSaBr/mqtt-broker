package javasabr.mqtt.legacy.handler.publish.out;

import javasabr.mqtt.model.QoS;
import javasabr.mqtt.legacy.network.MqttClient;
import javasabr.mqtt.legacy.network.packet.HasPacketId;
import javasabr.mqtt.legacy.network.packet.in.PublishAckInPacket;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Qos1PublishOutHandler extends PersistentPublishOutHandler {

  @Override
  protected QoS getQoS() {
    return QoS.AT_LEAST_ONCE;
  }

  @Override
  public boolean handleResponse(MqttClient client, HasPacketId response) {

    if (!(response instanceof PublishAckInPacket)) {
      throw new IllegalStateException("Unexpected response: " + response);
    }

    // just return 'true' to remove pending packet from session
    return true;
  }
}
