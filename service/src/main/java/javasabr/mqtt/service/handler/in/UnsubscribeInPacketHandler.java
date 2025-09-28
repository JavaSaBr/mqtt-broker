package javasabr.mqtt.service.handler.in;

import javasabr.mqtt.network.MqttClient.UnsafeMqttClient;
import javasabr.mqtt.network.packet.in.UnsubscribeInPacket;
import javasabr.mqtt.service.SubscriptionService;
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
