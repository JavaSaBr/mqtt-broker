package javasabr.mqtt.service.handler.in;

import static javasabr.mqtt.model.reason.code.SubscribeAckReasonCode.SHARED_SUBSCRIPTIONS_NOT_SUPPORTED;
import static javasabr.mqtt.model.reason.code.SubscribeAckReasonCode.WILDCARD_SUBSCRIPTIONS_NOT_SUPPORTED;
import static java.lang.Byte.toUnsignedInt;

import javasabr.mqtt.model.reason.code.DisconnectReasonCode;
import javasabr.mqtt.model.reason.code.SubscribeAckReasonCode;
import javasabr.mqtt.network.MqttClient.UnsafeMqttClient;
import javasabr.mqtt.network.packet.in.SubscribeInPacket;
import javasabr.mqtt.network.packet.out.MqttWritablePacket;
import javasabr.mqtt.service.SubscriptionService;
import java.util.Set;
import javasabr.rlib.collections.array.Array;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SubscribeInPacketHandler extends AbstractPacketHandler<UnsafeMqttClient, SubscribeInPacket> {

  private final static Set<SubscribeAckReasonCode> INVALID_ACK_CODE = Set.of(
      SHARED_SUBSCRIPTIONS_NOT_SUPPORTED,
      WILDCARD_SUBSCRIPTIONS_NOT_SUPPORTED);

  private final SubscriptionService subscriptionService;

  @Override
  protected void handleImpl(UnsafeMqttClient client, SubscribeInPacket packet) {

    Array<SubscribeAckReasonCode> ackReasonCodes = subscriptionService.subscribe(client, packet.getTopicFilters());
    MqttWritablePacket subscribeAck = client
        .packetOutFactory()
        .newSubscribeAck(packet.getPacketId(), ackReasonCodes);

    client.send(subscribeAck);

    SubscribeAckReasonCode anyReason = ackReasonCodes
        .reversedIterations()
        .findAny(INVALID_ACK_CODE, Set::contains);

    if (anyReason != null) {
      var disconnectReasonCode = DisconnectReasonCode.of(toUnsignedInt(anyReason.getValue()));
      MqttWritablePacket disconnect = client
          .packetOutFactory()
          .newDisconnect(client, disconnectReasonCode);

      client
          .sendWithFeedback(disconnect)
          .thenAccept(result -> client
              .connection()
              .close());
    }
  }
}
