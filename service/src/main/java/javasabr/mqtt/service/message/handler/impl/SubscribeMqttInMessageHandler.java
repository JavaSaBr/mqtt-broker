package javasabr.mqtt.service.message.handler.impl;

import static javasabr.mqtt.model.reason.code.SubscribeAckReasonCode.SHARED_SUBSCRIPTIONS_NOT_SUPPORTED;
import static javasabr.mqtt.model.reason.code.SubscribeAckReasonCode.WILDCARD_SUBSCRIPTIONS_NOT_SUPPORTED;

import java.util.Set;
import javasabr.mqtt.model.MqttClientConnectionConfig;
import javasabr.mqtt.model.MqttProperties;
import javasabr.mqtt.model.MqttServerConnectionConfig;
import javasabr.mqtt.model.reason.code.DisconnectReasonCode;
import javasabr.mqtt.model.reason.code.SubscribeAckReasonCode;
import javasabr.mqtt.model.subscribtion.RequestedSubscription;
import javasabr.mqtt.model.subscribtion.Subscription;
import javasabr.mqtt.network.MqttClient;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.impl.ExternalMqttClient;
import javasabr.mqtt.network.message.MqttMessageType;
import javasabr.mqtt.network.message.in.SubscribeMqttInMessage;
import javasabr.mqtt.service.MessageOutFactoryService;
import javasabr.mqtt.service.SubscriptionService;
import javasabr.mqtt.service.TopicService;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.collections.array.ArrayFactory;
import javasabr.rlib.collections.array.MutableArray;
import javasabr.rlib.common.util.StringUtils;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SubscribeMqttInMessageHandler extends
    AbstractMqttInMessageHandler<ExternalMqttClient, SubscribeMqttInMessage> {

  private final static Set<SubscribeAckReasonCode> DISCONNECT_CASES = Set.of(
      SHARED_SUBSCRIPTIONS_NOT_SUPPORTED,
      WILDCARD_SUBSCRIPTIONS_NOT_SUPPORTED);

  private static final Array<SubscribeAckReasonCode> SUBSCRIPTION_ID_NOT_SUPPORTED =
      Array.of(SubscribeAckReasonCode.SUBSCRIPTION_IDENTIFIERS_NOT_SUPPORTED);

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

    MqttClientConnectionConfig clientConfig = client.connectionConfig();
    MqttServerConnectionConfig serverConfig = clientConfig.server();

    if (message.subscriptionId() != MqttProperties.SUBSCRIPTION_ID_UNDEFINED) {
      if (!serverConfig.subscriptionIdAvailable()) {
        sendSubscriptionIdNotSupported(client, message);
        return;
      }
    }

    Array<Subscription> subscriptions = transformSubscriptions(
        client,
        message.subscriptions(),
        message.subscriptionId());

    Array<SubscribeAckReasonCode> subscriptionResults = subscriptionService
        .subscribe(client, subscriptions);

    sendSubscriptionResults(client, message, subscriptionResults);

    SubscribeAckReasonCode anyReasonToDisconnect = subscriptionResults
        .reversedIterations()
        .findAny(DISCONNECT_CASES, Set::contains);

    if (anyReasonToDisconnect != null) {
      DisconnectReasonCode reasonCode = DisconnectReasonCode.ofCode(anyReasonToDisconnect.code());
      client.closeWithReason(messageOutFactoryService
          .resolveFactory(client)
          .newDisconnect(client, reasonCode, message.userProperties()));
    }
  }

  private Array<Subscription> transformSubscriptions(
      MqttClient client,
      Array<RequestedSubscription> requestedSubscriptions,
      int subscriptionId) {

    MutableArray<Subscription> subscriptions =
        ArrayFactory.mutableArray(Subscription.class, requestedSubscriptions.size());

    for (RequestedSubscription requested : requestedSubscriptions) {
      String rawTopicFilter = requested.rawTopicFilter();
      subscriptions.add(new Subscription(
          topicService.createTopicFilter(client, rawTopicFilter),
          subscriptionId,
          requested.qos(),
          requested.retainHandling(),
          requested.noLocal(),
          requested.retainAsPublished()));
    }

    return subscriptions;
  }

  private void sendSubscriptionIdNotSupported(ExternalMqttClient client, SubscribeMqttInMessage message) {
    client.send(messageOutFactoryService
        .resolveFactory(client)
        .newSubscribeAck(
            message.messageId(),
            SUBSCRIPTION_ID_NOT_SUPPORTED,
            StringUtils.EMPTY,
            message.userProperties()));
  }

  private void sendSubscriptionResults(
      ExternalMqttClient client,
      SubscribeMqttInMessage message,
      Array<SubscribeAckReasonCode> subscriptionResults) {
    client.send(messageOutFactoryService
        .resolveFactory(client)
        .newSubscribeAck(message.messageId(), subscriptionResults, message.userProperties()));
  }
}
