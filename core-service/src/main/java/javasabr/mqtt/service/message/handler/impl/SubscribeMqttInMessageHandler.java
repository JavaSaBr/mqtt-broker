package javasabr.mqtt.service.message.handler.impl;

import static javasabr.mqtt.model.reason.code.SubscribeAckReasonCode.SHARED_SUBSCRIPTIONS_NOT_SUPPORTED;
import static javasabr.mqtt.model.reason.code.SubscribeAckReasonCode.WILDCARD_SUBSCRIPTIONS_NOT_SUPPORTED;

import java.util.Set;
import javasabr.mqtt.model.MqttClientConnectionConfig;
import javasabr.mqtt.model.MqttProperties;
import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.model.reason.code.DisconnectReasonCode;
import javasabr.mqtt.model.reason.code.SubscribeAckReasonCode;
import javasabr.mqtt.model.session.MessageTacker;
import javasabr.mqtt.model.subscription.RequestedSubscription;
import javasabr.mqtt.model.subscription.Subscription;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.impl.ExternalMqttClient;
import javasabr.mqtt.network.message.in.SubscribeMqttInMessage;
import javasabr.mqtt.network.message.out.MqttOutMessage;
import javasabr.mqtt.network.session.MqttNetworkSession;
import javasabr.mqtt.service.MessageOutFactoryService;
import javasabr.mqtt.service.SubscriptionService;
import javasabr.mqtt.service.TopicService;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.collections.array.ArrayFactory;
import javasabr.rlib.collections.array.MutableArray;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.experimental.FieldDefaults;

@CustomLog
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
  protected void processValidMessage(
      MqttConnection connection,
      ExternalMqttClient client,
      MqttNetworkSession session,
      SubscribeMqttInMessage subscribeMessage) {

    MqttClientConnectionConfig connectionConfig = client.connectionConfig();
    int messageId = subscribeMessage.messageId();
    MessageTacker messageTacker = session.inMessageTracker();
    if (messageTacker.stored(messageId) != null) {
      log.warning(client.clientId(), messageId, "[%s] MessageId:[%d] is already in use"::formatted);
      handleMessageIdIsInUse(client, subscribeMessage);
      return;
    }

    messageTacker.add(subscribeMessage.messageId(), MqttMessageType.SUBSCRIBE);

    int subscriptionId = subscribeMessage.subscriptionId();
    if (subscriptionId != MqttProperties.SUBSCRIPTION_ID_IS_NOT_SET) {
      if (!connectionConfig.subscriptionIdAvailable()) {
        log.warning(client.clientId(), subscriptionId,
            "[%s] Provided subscriptionId:[%d] but server doesn't allow it"::formatted);
        handleSubscriptionIdNotSupported(client, session, subscribeMessage);
        return;
      }
    }

    Array<Subscription> subscriptions = transformSubscriptions(
        connectionConfig,
        client,
        subscribeMessage.subscriptions(), subscriptionId);

    Array<SubscribeAckReasonCode> subscribeResults = subscriptionService
        .subscribe(client, session, subscriptions);

    sendSubscribeResults(client, session, subscribeMessage, subscribeResults);

    SubscribeAckReasonCode anyReasonToDisconnect = subscribeResults
        .iterations()
        .reversedArgs()
        .findAny(DISCONNECT_CASES, Set::contains);

    if (anyReasonToDisconnect != null) {
      log.info(client.clientId(), anyReasonToDisconnect, "[%s] Will be forced closing by reason:[%s]"::formatted);
      DisconnectReasonCode reasonCode = DisconnectReasonCode.ofCode(anyReasonToDisconnect.code());
      client.closeWithReason(messageOutFactoryService
          .resolveFactory(client)
          .newDisconnect(client, reasonCode));
    }
  }

  private Array<Subscription> transformSubscriptions(
      MqttClientConnectionConfig connectionConfig,
      ExternalMqttClient client,
      Array<RequestedSubscription> requestedSubscriptions,
      int subscriptionId) {

    QoS maxQos = connectionConfig.maxQos();
    MutableArray<Subscription> subscriptions =
        ArrayFactory.mutableArray(Subscription.class, requestedSubscriptions.size());

    for (RequestedSubscription requested : requestedSubscriptions) {
      TopicFilter topicFilter = topicService.createTopicFilter(client, requested.rawTopicFilter());
      QoS resultQos = maxQos.lower(requested.qos());
      subscriptions.add(new Subscription(
          topicFilter,
          subscriptionId,
          resultQos,
          requested.retainHandling(),
          requested.noLocal(),
          requested.retainAsPublished()));
    }

    return subscriptions;
  }

  private void handleMessageIdIsInUse(
      ExternalMqttClient client,
      SubscribeMqttInMessage subscribeMessage) {
    Array<SubscribeAckReasonCode> subscribeResults = Array.repeated(
        SubscribeAckReasonCode.PACKET_IDENTIFIER_IN_USE,
        subscribeMessage.subscriptionsCount());
    client.send(messageOutFactoryService
        .resolveFactory(client)
        .newSubscribeAck(subscribeMessage.messageId(), subscribeResults));
  }

  private void handleSubscriptionIdNotSupported(
      ExternalMqttClient client,
      MqttNetworkSession session,
      SubscribeMqttInMessage subscribeMessage) {
    Array<SubscribeAckReasonCode> subscribeResults = Array.repeated(
        SubscribeAckReasonCode.SUBSCRIPTION_IDENTIFIERS_NOT_SUPPORTED,
        subscribeMessage.subscriptionsCount());
    int messageId = subscribeMessage.messageId();
    MqttOutMessage response = messageOutFactoryService
        .resolveFactory(client)
        .newSubscribeAck(messageId, subscribeResults);
    client.sendWithFeedback(response)
        .thenAccept(_ -> session
            .inMessageTracker()
            .remove(messageId));
  }

  private void sendSubscribeResults(
      ExternalMqttClient client,
      MqttNetworkSession session,
      SubscribeMqttInMessage subscribeMessage,
      Array<SubscribeAckReasonCode> subscribeResults) {
    int messageId = subscribeMessage.messageId();
    MqttOutMessage response = messageOutFactoryService
        .resolveFactory(client)
        .newSubscribeAck(messageId, subscribeResults);
    client.sendWithFeedback(response)
        .thenAccept(_ -> session
            .inMessageTracker()
            .remove(messageId));
  }
}
