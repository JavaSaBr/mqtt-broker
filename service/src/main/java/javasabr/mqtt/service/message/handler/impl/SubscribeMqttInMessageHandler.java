package javasabr.mqtt.service.message.handler.impl;

import static java.lang.Byte.toUnsignedInt;
import static javasabr.mqtt.model.reason.code.SubscribeAckReasonCode.SHARED_SUBSCRIPTIONS_NOT_SUPPORTED;
import static javasabr.mqtt.model.reason.code.SubscribeAckReasonCode.WILDCARD_SUBSCRIPTIONS_NOT_SUPPORTED;

import java.util.Set;
import javasabr.mqtt.model.reason.code.DisconnectReasonCode;
import javasabr.mqtt.model.reason.code.SubscribeAckReasonCode;
import javasabr.mqtt.model.subscribtion.RequestedRawSubscription;
import javasabr.mqtt.model.subscribtion.Subscription;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.mqtt.network.MqttClient;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.impl.ExternalMqttClient;
import javasabr.mqtt.network.message.MqttMessageType;
import javasabr.mqtt.network.message.in.SubscribeMqttInMessage;
import javasabr.mqtt.network.message.out.MqttOutMessage;
import javasabr.mqtt.service.MessageOutFactoryService;
import javasabr.mqtt.service.SubscriptionService;
import javasabr.mqtt.service.TopicService;
import javasabr.mqtt.service.message.out.factory.MqttMessageOutFactory;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.collections.array.ArrayFactory;
import javasabr.rlib.collections.array.MutableArray;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SubscribeMqttInMessageHandler extends
    AbstractMqttInMessageHandler<ExternalMqttClient, SubscribeMqttInMessage> {

  private final static Set<SubscribeAckReasonCode> INVALID_ACK_CODE = Set.of(
      SHARED_SUBSCRIPTIONS_NOT_SUPPORTED,
      WILDCARD_SUBSCRIPTIONS_NOT_SUPPORTED);

  SubscriptionService subscriptionService;
  TopicService topicService;

  public SubscribeMqttInMessageHandler(
      SubscriptionService subscriptionService,
      MessageOutFactoryService messageOutFactoryService,
      TopicService topicService) {
    super(ExternalMqttClient.class, SubscribeMqttInMessage.class, messageOutFactoryService);
    this.subscriptionService = subscriptionService;
    this.topicService = topicService;
  }

  @Override
  public MqttMessageType messageType() {
    return MqttMessageType.SUBSCRIBE;
  }

  @Override
  protected void processReceived(
      MqttConnection connection,
      ExternalMqttClient client,
      SubscribeMqttInMessage message) {

    Array<RequestedRawSubscription> rawSubscriptions = message.subscriptions();
    Array<Subscription> subscriptions = transformSubscriptions(client, rawSubscriptions);

    MqttMessageOutFactory messageOutFactory = messageOutFactoryService.resolveFactory(client);
    Array<SubscribeAckReasonCode> ackReasonCodes = subscriptionService
        .subscribe(client, subscriptions);

    MqttOutMessage subscribeAck = messageOutFactory
        .newSubscribeAck(message.messageId(), ackReasonCodes);

    client.send(subscribeAck);

    SubscribeAckReasonCode anyReason = ackReasonCodes
        .reversedIterations()
        .findAny(INVALID_ACK_CODE, Set::contains);

    if (anyReason != null) {
      var disconnectReasonCode = DisconnectReasonCode.of(toUnsignedInt(anyReason.getValue()));
      MqttOutMessage disconnect = messageOutFactory
          .newDisconnect(client, disconnectReasonCode);

      client
          .sendWithFeedback(disconnect)
          .thenAccept(_ -> client
              .connection()
              .close());
    }
  }

  private Array<Subscription> transformSubscriptions(MqttClient client, Array<RequestedRawSubscription> rawSubscriptions) {
    MutableArray<Subscription> subscriptions =
        ArrayFactory.mutableArray(Subscription.class, rawSubscriptions.size());

    for (RequestedRawSubscription subscription : rawSubscriptions) {
      String rawTopicFilter = subscription.topicFilter();
      TopicFilter topicFilter = topicService.createTopicFilter(client, rawTopicFilter);
      subscriptions.add(new Subscription(
          topicFilter,
          subscription.qos(),
          subscription.retainHandling(),
          subscription.noLocal(),
          subscription.retainAsPublished()));
    }

    return subscriptions;
  }

  private void sendUnspecifiedError(ExternalMqttClient client, SubscribeMqttInMessage message) {
    client.send(messageOutFactoryService
        .resolveFactory(client)
        .newSubscribeAck(message.messageId(), Array.of(SubscribeAckReasonCode.UNSPECIFIED_ERROR)));
  }
}
