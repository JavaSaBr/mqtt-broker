package javasabr.mqtt.legacy.handler.packet.in;

import javasabr.mqtt.legacy.network.MqttClient.UnsafeMqttClient;
import javasabr.mqtt.legacy.network.packet.in.UnsubscribeInPacket;
import javasabr.mqtt.legacy.service.SubscriptionService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UnsubscribeInPacketHandler extends AbstractPacketHandler<UnsafeMqttClient, UnsubscribeInPacket> {

  private final SubscriptionService subscriptionService;

  @Override
  protected void handleImpl(UnsafeMqttClient client, UnsubscribeInPacket packet) {
    var ackReasonCodes = subscriptionService.unsubscribe(client, packet.getTopicFilters());
    client.send(client
        .getPacketOutFactory()
        .newUnsubscribeAck(packet.getPacketId(), ackReasonCodes));
  }
}
