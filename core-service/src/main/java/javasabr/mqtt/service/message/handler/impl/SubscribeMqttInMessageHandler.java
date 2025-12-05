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
import javasabr.mqtt.network.impl.ExternalNetworkMqttUser;
import javasabr.mqtt.network.message.in.SubscribeMqttInMessage;
import javasabr.mqtt.network.message.out.MqttOutMessage;
import javasabr.mqtt.network.session.NetworkMqttSession;
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
    AbstractMqttInMessageHandler<ExternalNetworkMqttUser, SubscribeMqttInMessage> {

  private final static Set<SubscribeAckReasonCode> DISCONNECT_CASES = Set.of(
      SHARED_SUBSCRIPTIONS_NOT_SUPPORTED,
      WILDCARD_SUBSCRIPTIONS_NOT_SUPPORTED);

  SubscriptionService subscriptionService;
  TopicService topicService;

  public SubscribeMqttInMessageHandler(
      SubscriptionService subscriptionService,
      MessageOutFactoryService messageOutFactoryService,
      TopicService topicService) {
    super(ExternalNetworkMqttUser.class, SubscribeMqttInMessage.class, messageOutFactoryService);
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

    Array<SubscribeAckReasonCode> subscribeResults = subscriptionService
        .subscribe(user, session, subscriptions);

    sendSubscribeResults(user, session, subscribeMessage, subscribeResults);

    SubscribeAckReasonCode anyReasonToDisconnect = subscribeResults
        .iterations()
        .reversedArgs()
        .findAny(DISCONNECT_CASES, Set::contains);

    if (anyReasonToDisconnect != null) {
      log.info(user.clientId(), anyReasonToDisconnect, "[%s] Will be forced closing by reason:[%s]"::formatted);
      DisconnectReasonCode reasonCode = DisconnectReasonCode.ofCode(anyReasonToDisconnect.code());
      user.closeWithReason(messageOutFactoryService
          .resolveFactory(user)
          .newDisconnect(user, reasonCode));
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
      Array<SubscribeAckReasonCode> subscribeResults) {
    int messageId = subscribeMessage.messageId();
    MqttOutMessage response = messageOutFactoryService
        .resolveFactory(user)
        .newSubscribeAck(messageId, subscribeResults);
    user.sendAsync(response)
        .thenAccept(_ -> session
            .inMessageTracker()
            .remove(messageId));
  }
}
