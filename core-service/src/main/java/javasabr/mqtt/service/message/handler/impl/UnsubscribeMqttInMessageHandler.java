package javasabr.mqtt.service.message.handler.impl;

import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.model.reason.code.UnsubscribeAckReasonCode;
import javasabr.mqtt.model.session.MessageTacker;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.MqttNetworkSession;
import javasabr.mqtt.network.impl.ExternalNetworkMqttUser;
import javasabr.mqtt.network.message.in.UnsubscribeMqttInMessage;
import javasabr.mqtt.network.message.out.MqttOutMessage;
import javasabr.mqtt.service.MessageOutFactoryService;
import javasabr.mqtt.service.SubscriptionService;
import javasabr.mqtt.service.TopicService;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.collections.array.ArrayCollectors;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.experimental.FieldDefaults;

@CustomLog
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UnsubscribeMqttInMessageHandler
    extends AbstractMqttInMessageHandler<ExternalNetworkMqttUser, UnsubscribeMqttInMessage> {

  SubscriptionService subscriptionService;
  TopicService topicService;

  public UnsubscribeMqttInMessageHandler(
      SubscriptionService subscriptionService,
      MessageOutFactoryService messageOutFactoryService,
      TopicService topicService) {
    super(ExternalNetworkMqttUser.class, UnsubscribeMqttInMessage.class, messageOutFactoryService);
    this.subscriptionService = subscriptionService;
    this.topicService = topicService;
  }

  @Override
  public MqttMessageType messageType() {
    return MqttMessageType.UNSUBSCRIBE;
  }

  @Override
  protected void processValidMessage(
      MqttConnection connection,
      ExternalNetworkMqttUser user,
      MqttNetworkSession session,
      UnsubscribeMqttInMessage unsubscribeMessage) {

    int messageId = unsubscribeMessage.messageId();
    MessageTacker messageTacker = session.inMessageTracker();
    if (messageTacker.stored(messageId) != null) {
      log.warning(user.clientId(), messageId, "[%s] MessageId:[%d] is already in use"::formatted);
      handleMessageIdIsInUse(user, unsubscribeMessage);
      return;
    }

    messageTacker.add(messageId, MqttMessageType.UNSUBSCRIBE);

    Array<TopicFilter> topicFilters = unsubscribeMessage
        .rawTopicFilters()
        .stream()
        .map(rawTopicFilter -> topicService.createTopicFilter(user, rawTopicFilter))
        .collect(ArrayCollectors.toArray(TopicFilter.class));

    Array<UnsubscribeAckReasonCode> unsubscribeResults = subscriptionService
        .unsubscribe(user, session, topicFilters);

    MqttOutMessage response = messageOutFactoryService
        .resolveFactory(user)
        .newUnsubscribeAck(unsubscribeMessage.messageId(), unsubscribeResults);

    user
        .sendWithFeedback(response)
        .thenAccept(_ -> session
            .inMessageTracker()
            .remove(messageId));
  }

  private void handleMessageIdIsInUse(
      ExternalNetworkMqttUser user,
      UnsubscribeMqttInMessage unsubscribeMessage) {
    Array<UnsubscribeAckReasonCode> unsubscribeResults = Array.repeated(
        UnsubscribeAckReasonCode.PACKET_IDENTIFIER_IN_USE,
        unsubscribeMessage.topicFiltersCount());
    user.send(messageOutFactoryService
        .resolveFactory(user)
        .newUnsubscribeAck(unsubscribeMessage.messageId(), unsubscribeResults));
  }
}
