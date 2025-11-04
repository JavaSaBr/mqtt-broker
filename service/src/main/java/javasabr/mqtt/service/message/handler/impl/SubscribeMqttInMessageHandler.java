package javasabr.mqtt.service.message.handler.impl;

import static javasabr.mqtt.model.reason.code.SubscribeAckReasonCode.SHARED_SUBSCRIPTIONS_NOT_SUPPORTED;
import static javasabr.mqtt.model.reason.code.SubscribeAckReasonCode.WILDCARD_SUBSCRIPTIONS_NOT_SUPPORTED;

import java.util.Set;
import javasabr.mqtt.model.reason.code.DisconnectReasonCode;
import javasabr.mqtt.model.reason.code.SubscribeAckReasonCode;
import javasabr.mqtt.model.subscribtion.RequestedSubscription;
import javasabr.mqtt.model.subscribtion.Subscription;
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

  private final static Set<SubscribeAckReasonCode> DISCONNECT_CASES = Set.of(
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

    Array<Subscription> subscriptions = transformSubscriptions(client, message.subscriptions());

    MqttMessageOutFactory messageOutFactory = messageOutFactoryService.resolveFactory(client);
    Array<SubscribeAckReasonCode> ackReasonCodes = subscriptionService
        .subscribe(client, subscriptions);

    MqttOutMessage subscribeAck = messageOutFactory
        .newSubscribeAck(message.messageId(), ackReasonCodes);

    client.send(subscribeAck);

    SubscribeAckReasonCode anyReasonToDisconnect = ackReasonCodes
        .reversedIterations()
        .findAny(DISCONNECT_CASES, Set::contains);

    if (anyReasonToDisconnect != null) {
      MqttOutMessage disconnect = messageOutFactory
          .newDisconnect(client, DisconnectReasonCode.ofCode(anyReasonToDisconnect.code()));
      client.closeWithReason(disconnect);
    }
  }

  private Array<Subscription> transformSubscriptions(
      MqttClient client,
      Array<RequestedSubscription> requestedSubscriptions) {

    MutableArray<Subscription> subscriptions =
        ArrayFactory.mutableArray(Subscription.class, requestedSubscriptions.size());

    for (RequestedSubscription requested : requestedSubscriptions) {
      String rawTopicFilter = requested.rawTopicFilter();
      subscriptions.add(new Subscription(
          topicService.createTopicFilter(client, rawTopicFilter),
          requested.qos(),
          requested.retainHandling(),
          requested.noLocal(),
          requested.retainAsPublished()));
    }

    return subscriptions;
  }
}
