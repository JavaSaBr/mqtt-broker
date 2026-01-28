package javasabr.mqtt.service.message.handler.impl;

import static javasabr.mqtt.model.SubscribeRetainHandling.SEND;
import static javasabr.mqtt.model.SubscribeRetainHandling.SEND_IF_SUBSCRIPTION_DOES_NOT_EXIST;
import static javasabr.mqtt.model.reason.code.SubscribeAckReasonCode.SHARED_SUBSCRIPTIONS_NOT_SUPPORTED;
import static javasabr.mqtt.model.reason.code.SubscribeAckReasonCode.WILDCARD_SUBSCRIPTIONS_NOT_SUPPORTED;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import javasabr.mqtt.model.MqttClientConnectionConfig;
import javasabr.mqtt.model.MqttProperties;
import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.SubscribeRetainHandling;
import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.model.reason.code.DisconnectReasonCode;
import javasabr.mqtt.model.reason.code.SubscribeAckReasonCode;
import javasabr.mqtt.model.session.MessageTacker;
import javasabr.mqtt.model.subscription.RequestedSubscription;
import javasabr.mqtt.model.subscription.Subscription;
import javasabr.mqtt.model.subscription.SubscriptionResult;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.impl.ExternalNetworkMqttUser;
import javasabr.mqtt.network.message.in.SubscribeMqttInMessage;
import javasabr.mqtt.network.message.out.MqttOutMessage;
import javasabr.mqtt.network.session.NetworkMqttSession;
import javasabr.mqtt.service.MessageOutFactoryService;
import javasabr.mqtt.service.PublishDeliveringService;
import javasabr.mqtt.service.RetainMessageService;
import javasabr.mqtt.service.SubscriptionService;
import javasabr.mqtt.service.TopicService;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.collections.array.ArrayCollectors;
import javasabr.rlib.collections.array.ArrayFactory;
import javasabr.rlib.collections.array.MutableArray;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.experimental.FieldDefaults;

@CustomLog
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SubscribeMqttInMessageHandler extends
    AbstractMqttInMessageHandler<ExternalNetworkMqttUser, SubscribeMqttInMessage> {

  private final static Set<SubscribeAckReasonCode> DISCONNECT_CASES = Set.of(
      SHARED_SUBSCRIPTIONS_NOT_SUPPORTED,
      WILDCARD_SUBSCRIPTIONS_NOT_SUPPORTED);

  SubscriptionService subscriptionService;
  TopicService topicService;
  RetainMessageService retainMessageService;
  PublishDeliveringService publishDeliveringService;

  public SubscribeMqttInMessageHandler(
      SubscriptionService subscriptionService,
      MessageOutFactoryService messageOutFactoryService,
      TopicService topicService,
      RetainMessageService retainMessageService,
      PublishDeliveringService publishDeliveringService) {
    super(ExternalNetworkMqttUser.class, SubscribeMqttInMessage.class, messageOutFactoryService);
    this.subscriptionService = subscriptionService;
    this.topicService = topicService;
    this.retainMessageService = retainMessageService;
    this.publishDeliveringService = publishDeliveringService;
  }

  @Override
  public MqttMessageType messageType() {
    return MqttMessageType.SUBSCRIBE;
  }

  @Override
  protected void processValidMessage(
      MqttConnection connection,
      ExternalNetworkMqttUser user,
      NetworkMqttSession session,
      SubscribeMqttInMessage subscribeMessage) {

    MqttClientConnectionConfig connectionConfig = user.connectionConfig();
    int messageId = subscribeMessage.messageId();
    MessageTacker messageTacker = session.inMessageTracker();
    if (messageTacker.stored(messageId) != null) {
      log.warning(user.clientId(), messageId, "[%s] MessageId:[%d] is already in use"::formatted);
      handleMessageIdIsInUse(user, subscribeMessage);
      return;
    }

    messageTacker.add(subscribeMessage.messageId(), MqttMessageType.SUBSCRIBE);

    int subscriptionId = subscribeMessage.subscriptionId();
    if (subscriptionId != MqttProperties.SUBSCRIPTION_ID_IS_NOT_SET) {
      if (!connectionConfig.subscriptionIdAvailable()) {
        log.warning(
            user.clientId(), subscriptionId,
            "[%s] Provided subscriptionId:[%d] but server doesn't allow it"::formatted);
        handleSubscriptionIdNotSupported(user, session, subscribeMessage);
        return;
      }
    }

    Array<Subscription> subscriptions = transformSubscriptions(
        connectionConfig, 
        user,
        subscribeMessage.subscriptions(),
        subscriptionId);

    Array<SubscriptionResult> subscribeResults = subscriptionService
        .subscribe(user, session, subscriptions);

    sendSubscribeResults(user, session, subscribeMessage, subscribeResults);
    
    SubscriptionResult anyDisconnectResult = subscribeResults
        .iterations()
        .reversedArgs()
        .findAny(DISCONNECT_CASES, (codes, candidate) -> codes.contains(candidate.subscribeAckReasonCode()));

    if (anyDisconnectResult != null) {
      SubscribeAckReasonCode reasonCode = anyDisconnectResult.subscribeAckReasonCode();
      log.info(user.clientId(), reasonCode, "[%s] Will be forced closing by reason:[%s]"::formatted);
      user.closeWithReason(messageOutFactoryService
          .resolveFactory(user)
          .newDisconnect(user, DisconnectReasonCode.ofCode(reasonCode.code())));
    } else {
      // No sense sending retained messages if we are going to disconnect this client
      sendRetainedMessages(user, subscribeResults);
    }
  }

  private Array<Subscription> transformSubscriptions(
      MqttClientConnectionConfig connectionConfig,
      ExternalNetworkMqttUser user,
      Array<RequestedSubscription> requestedSubscriptions,
      int subscriptionId) {

    QoS maxQos = connectionConfig.maxQos();
    MutableArray<Subscription> subscriptions =
        ArrayFactory.mutableArray(Subscription.class, requestedSubscriptions.size());

    for (RequestedSubscription requested : requestedSubscriptions) {
      TopicFilter topicFilter = topicService.createTopicFilter(user, requested.rawTopicFilter());
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
      ExternalNetworkMqttUser user,
      SubscribeMqttInMessage subscribeMessage) {
    Array<SubscribeAckReasonCode> subscribeResults = Array.repeated(
        SubscribeAckReasonCode.PACKET_IDENTIFIER_IN_USE,
        subscribeMessage.subscriptionsCount());
    user.sendInBackground(messageOutFactoryService
        .resolveFactory(user)
        .newSubscribeAck(subscribeMessage.messageId(), subscribeResults));
  }

  private void handleSubscriptionIdNotSupported(
      ExternalNetworkMqttUser user,
      NetworkMqttSession session,
      SubscribeMqttInMessage subscribeMessage) {
    Array<SubscribeAckReasonCode> subscribeResults = Array.repeated(
        SubscribeAckReasonCode.SUBSCRIPTION_IDENTIFIERS_NOT_SUPPORTED,
        subscribeMessage.subscriptionsCount());
    int messageId = subscribeMessage.messageId();
    MqttOutMessage response = messageOutFactoryService
        .resolveFactory(user)
        .newSubscribeAck(messageId, subscribeResults);
    user.sendAsync(response)
        .thenAccept(_ -> session
            .inMessageTracker()
            .remove(messageId));
  }

  private void sendSubscribeResults(
      ExternalNetworkMqttUser user,
      NetworkMqttSession session,
      SubscribeMqttInMessage subscribeMessage,
      Array<SubscriptionResult> subscribeResults) {
    int messageId = subscribeMessage.messageId();
    Array<SubscribeAckReasonCode> ackReasonCodes = subscribeResults.stream()
        .map(SubscriptionResult::subscribeAckReasonCode)
        .collect(ArrayCollectors.toArray(SubscribeAckReasonCode.class));
    MqttOutMessage response = messageOutFactoryService
        .resolveFactory(user)
        .newSubscribeAck(messageId, ackReasonCodes);
    user.sendAsync(response)
        .thenAccept(_ -> session
            .inMessageTracker()
            .remove(messageId));
  }

  private void sendRetainedMessages(MqttUser user, Array<SubscriptionResult> subscribeResults) {
    if (subscribeResults.isEmpty()) {
      return;
    }
    Map<Publish, Subscription> uniqueRetainedMessages = null;
    for (SubscriptionResult subscriptionResult : subscribeResults) {
      Subscription subscription = subscriptionResult.newSubscription();
      if (subscription == null || !isRetainHandlingRequired(subscription, subscriptionResult)) {
        continue;
      }
      boolean retainAsPublished = subscription.retainAsPublished();
      Array<Publish> retainedMessages = retainMessageService.findRetainedMessages(subscription.topicFilter());
      for (Publish retainedMessage : retainedMessages) {
        if (!retainAsPublished) {
          retainedMessage = retainedMessage.withoutRetain();
        }
        if (uniqueRetainedMessages == null) {
          uniqueRetainedMessages = new IdentityHashMap<>();
        }
        uniqueRetainedMessages.merge(retainedMessage, subscription, Subscription::higherQoS);
      }
    }
    if (uniqueRetainedMessages == null) {
      return;
    }
    for (Map.Entry<Publish, Subscription> retainedMessageEntry : uniqueRetainedMessages.entrySet()) {
      publishDeliveringService.startDelivering(
          retainedMessageEntry.getKey(),
          user,
          retainedMessageEntry.getValue());
    }
  }

  private static boolean isRetainHandlingRequired(Subscription subscription, SubscriptionResult subscriptionResult) {
    if (subscription.topicFilter().isShared()) {
      return false;
    } else {
      SubscribeRetainHandling retainHandling = subscription.retainHandling();
      return retainHandling == SEND || (retainHandling == SEND_IF_SUBSCRIPTION_DOES_NOT_EXIST
                                            && subscriptionResult.isNotExistedPreviously());
    }
  }
}
