package javasabr.mqtt.service.publish.handler.impl;

import static javasabr.mqtt.model.reason.code.PublishReleaseReasonCode.SUCCESS;

import javasabr.mqtt.model.QoS;
import javasabr.mqtt.network.MqttClient;
import javasabr.mqtt.network.packet.HasPacketId;
import javasabr.mqtt.network.packet.in.PublishCompleteInPacket;
import javasabr.mqtt.network.packet.in.PublishReceivedInPacket;
import javasabr.mqtt.service.SubscriptionService;

public class Qos2MqttPublishOutMessageHandler extends PersistedMqttPublishOutMessageHandler {

  public Qos2MqttPublishOutMessageHandler(SubscriptionService subscriptionService) {
    super(subscriptionService);
  }

  @Override
  public QoS qos() {
    return QoS.EXACTLY_ONCE;
  }

  @Override
  protected boolean handleReceivedResponse(MqttClient client, HasPacketId response) {
    var packetOutFactory = client.packetOutFactory();
    if (response instanceof PublishReceivedInPacket) {
      client.send(packetOutFactory.newPublishRelease(response.packetId(), SUCCESS));
      return false;
    } else if (response instanceof PublishCompleteInPacket) {
      return true;
    } else {
      throw new IllegalStateException("Unexpected response: " + response);
    }
  }
}
