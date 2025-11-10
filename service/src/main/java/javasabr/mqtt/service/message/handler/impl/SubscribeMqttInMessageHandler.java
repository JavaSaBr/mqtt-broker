package javasabr.mqtt.service.message.handler.impl;

import static javasabr.mqtt.model.reason.code.SubscribeAckReasonCode.SHARED_SUBSCRIPTIONS_NOT_SUPPORTED;
import static javasabr.mqtt.model.reason.code.SubscribeAckReasonCode.WILDCARD_SUBSCRIPTIONS_NOT_SUPPORTED;

import java.util.Set;
import javasabr.mqtt.model.MqttClientConnectionConfig;
import javasabr.mqtt.model.MqttProperties;
import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.reason.code.DisconnectReasonCode;
import javasabr.mqtt.model.reason.code.SubscribeAckReasonCode;
import javasabr.mqtt.model.subscribtion.RequestedSubscription;
import javasabr.mqtt.model.subscribtion.Subscription;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.impl.ExternalMqttClient;
import javasabr.mqtt.network.message.MqttMessageType;
import javasabr.mqtt.network.message.in.SubscribeMqttInMessage;
import javasabr.mqtt.network.message.out.MqttOutMessage;
import javasabr.mqtt.network.session.MessageTacker;
import javasabr.mqtt.network.session.MqttSession;
import javasabr.mqtt.network.util.ExtraErrorReasons;
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
  protected void processReceivedValidMessage(
      MqttConnection connection,
      ExternalMqttClient client,
      SubscribeMqttInMessage subscribeMessage) {

    MqttClientConnectionConfig connectionConfig = client.connectionConfig();
    MqttSession session = client.session();
    if (session == null) {
      log.warning(client.clientId(), "[%s] Session is already closed"::formatted);
      handleSessionIsAlreadyClosed(client);
      return;
    }

    int messageId = subscribeMessage.messageId();
    MessageTacker messageTacker = session.inMessageTracker();
    if (messageTacker.isInUse(messageId)) {
      log.warning(client.clientId(), messageId, "[%s] Message id:[%d] is already in use"::formatted);
      handleMessageIdIsInUse(client, subscribeMessage);
      return;
    }

    messageTacker.add(subscribeMessage.messageId());

    int subscriptionId = subscribeMessage.subscriptionId();
    if (subscriptionId != MqttProperties.SUBSCRIPTION_ID_IS_NOT_SET) {
      if (!connectionConfig.subscriptionIdAvailable()) {
        log.warning(client.clientId(), subscriptionId,
            "[%s] Provided subscription id:[%d] but server doesn't allow it"::formatted);
        handleSubscriptionIdNotSupported(client, session, subscribeMessage);
        return;
      }
    }

    Array<Subscription> subscriptions = transformSubscriptions(
        connectionConfig,
        client,
        subscribeMessage.subscriptions(), subscriptionId);

    Array<SubscribeAckReasonCode> subscriptionResults = subscriptionService
        .subscribe(client, session, subscriptions);

    sendSubscriptionResults(client, session, subscribeMessage, subscriptionResults);

    SubscribeAckReasonCode anyReasonToDisconnect = subscriptionResults
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

  private void handleSessionIsAlreadyClosed(ExternalMqttClient client) {
    MqttOutMessage response = messageOutFactoryService
        .resolveFactory(client)
        .newDisconnect(
            client,
            DisconnectReasonCode.UNSPECIFIED_ERROR,
            ExtraErrorReasons.SESSION_IS_ALREADY_CLOSED);
    client.closeWithReason(response);
  }

  private void handleMessageIdIsInUse(
      ExternalMqttClient client,
      SubscribeMqttInMessage subscribeMessage) {
    Array<SubscribeAckReasonCode> subscriptionResults = Array.repeated(
        SubscribeAckReasonCode.PACKET_IDENTIFIER_IN_USE,
        subscribeMessage.subscriptionsCount());
    client.send(messageOutFactoryService
        .resolveFactory(client)
        .newSubscribeAck(subscribeMessage.messageId(), subscriptionResults));
  }

  private void handleSubscriptionIdNotSupported(
      ExternalMqttClient client,
      MqttSession session,
      SubscribeMqttInMessage subscribeMessage) {
    Array<SubscribeAckReasonCode> subscriptionResults = Array.repeated(
        SubscribeAckReasonCode.SUBSCRIPTION_IDENTIFIERS_NOT_SUPPORTED,
        subscribeMessage.subscriptionsCount());
    int messageId = subscribeMessage.messageId();
    MqttOutMessage response = messageOutFactoryService
        .resolveFactory(client)
        .newSubscribeAck(messageId, subscriptionResults);
    client.sendWithFeedback(response)
        .thenAccept(_ -> session
            .inMessageTracker()
            .remove(messageId));
  }

  private void sendSubscriptionResults(
      ExternalMqttClient client,
      MqttSession session,
      SubscribeMqttInMessage subscribeMessage,
      Array<SubscribeAckReasonCode> subscriptionResults) {
    int messageId = subscribeMessage.messageId();
    MqttOutMessage response = messageOutFactoryService
        .resolveFactory(client)
        .newSubscribeAck(messageId, subscriptionResults);
    client.sendWithFeedback(response)
        .thenAccept(_ -> session
            .inMessageTracker()
            .remove(messageId));
  }
}
