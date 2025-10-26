package javasabr.mqtt.service.message.handler.impl;

import static java.lang.Byte.toUnsignedInt;
import static javasabr.mqtt.model.reason.code.SubscribeAckReasonCode.SHARED_SUBSCRIPTIONS_NOT_SUPPORTED;
import static javasabr.mqtt.model.reason.code.SubscribeAckReasonCode.WILDCARD_SUBSCRIPTIONS_NOT_SUPPORTED;

import java.util.Set;
import javasabr.mqtt.model.reason.code.DisconnectReasonCode;
import javasabr.mqtt.model.reason.code.SubscribeAckReasonCode;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.client.ExternalMqttClient;
import javasabr.mqtt.network.packet.MqttPacketType;
import javasabr.mqtt.network.packet.in.SubscribeInPacket;
import javasabr.mqtt.network.packet.out.MqttWritablePacket;
import javasabr.mqtt.service.MessageOutFactoryService;
import javasabr.mqtt.service.SubscriptionService;
import javasabr.rlib.collections.array.Array;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SubscribeMqttInMessageHandler extends
    AbstractMqttInMessageHandler<ExternalMqttClient, SubscribeInPacket> {

  private final static Set<SubscribeAckReasonCode> INVALID_ACK_CODE = Set.of(
      SHARED_SUBSCRIPTIONS_NOT_SUPPORTED,
      WILDCARD_SUBSCRIPTIONS_NOT_SUPPORTED);

  SubscriptionService subscriptionService;
  MessageOutFactoryService messageOutFactoryService;

  public SubscribeMqttInMessageHandler(
      SubscriptionService subscriptionService,
      MessageOutFactoryService messageOutFactoryService) {
    super(ExternalMqttClient.class, SubscribeInPacket.class);
    this.subscriptionService = subscriptionService;
    this.messageOutFactoryService = messageOutFactoryService;
  }

  @Override
  public MqttPacketType messageType() {
    return MqttPacketType.SUBSCRIBE;
  }

  @Override
  protected void processReceived(
      MqttConnection connection,
      ExternalMqttClient client,
      SubscribeInPacket networkPacket) {

    Array<SubscribeAckReasonCode> ackReasonCodes = subscriptionService
        .subscribe(client, networkPacket.getTopicFilters());
    MqttWritablePacket subscribeAck = messageOutFactoryService
        .resolveFactory(client)
        .newSubscribeAck(networkPacket.getPacketId(), ackReasonCodes);

    client.send(subscribeAck);

    SubscribeAckReasonCode anyReason = ackReasonCodes
        .reversedIterations()
        .findAny(INVALID_ACK_CODE, Set::contains);

    if (anyReason != null) {
      var disconnectReasonCode = DisconnectReasonCode.of(toUnsignedInt(anyReason.getValue()));
      MqttWritablePacket disconnect = messageOutFactoryService
          .resolveFactory(client)
          .newDisconnect(client, disconnectReasonCode);

      client
          .sendWithFeedback(disconnect)
          .thenAccept(_ -> client
              .connection()
              .close());
    }
  }
}
