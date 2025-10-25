package javasabr.mqtt.service.message.handler.impl;

import javasabr.mqtt.model.reason.code.UnsubscribeAckReasonCode;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.impl.ExternalMqttClient;
import javasabr.mqtt.network.message.MqttMessageType;
import javasabr.mqtt.network.packet.in.UnsubscribeInPacket;
import javasabr.mqtt.service.MessageOutFactoryService;
import javasabr.mqtt.service.SubscriptionService;
import javasabr.rlib.collections.array.Array;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UnsubscribeMqttInMessageHandler extends AbstractMqttInMessageHandler<ExternalMqttClient, UnsubscribeInPacket> {

  SubscriptionService subscriptionService;
  MessageOutFactoryService messageOutFactoryService;

  public UnsubscribeMqttInMessageHandler(
      SubscriptionService subscriptionService,
      MessageOutFactoryService messageOutFactoryService) {
    super(ExternalMqttClient.class, UnsubscribeInPacket.class);
    this.subscriptionService = subscriptionService;
    this.messageOutFactoryService = messageOutFactoryService;
  }

  @Override
  public MqttMessageType messageType() {
    return MqttMessageType.UNSUBSCRIBE;
  }

  @Override
  protected void processReceived(
      MqttConnection connection,
      ExternalMqttClient client,
      UnsubscribeInPacket networkPacket) {

    Array<UnsubscribeAckReasonCode> ackReasonCodes = subscriptionService
        .unsubscribe(client, networkPacket.getTopicFilters());

    client.send(messageOutFactoryService
        .resolveFactory(client)
        .newUnsubscribeAck(networkPacket.getPacketId(), ackReasonCodes));
  }
}
