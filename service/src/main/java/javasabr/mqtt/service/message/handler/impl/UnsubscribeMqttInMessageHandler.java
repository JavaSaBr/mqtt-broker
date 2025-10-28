package javasabr.mqtt.service.message.handler.impl;

import javasabr.mqtt.model.reason.code.UnsubscribeAckReasonCode;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.impl.ExternalMqttClient;
import javasabr.mqtt.network.message.MqttMessageType;
import javasabr.mqtt.network.message.in.UnsubscribeMqttInMessage;
import javasabr.mqtt.service.MessageOutFactoryService;
import javasabr.mqtt.service.SubscriptionService;
import javasabr.mqtt.service.TopicService;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.collections.array.ArrayCollectors;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UnsubscribeMqttInMessageHandler
    extends AbstractMqttInMessageHandler<ExternalMqttClient, UnsubscribeMqttInMessage> {

  SubscriptionService subscriptionService;
  TopicService topicService;

  public UnsubscribeMqttInMessageHandler(
      SubscriptionService subscriptionService,
      MessageOutFactoryService messageOutFactoryService,
      TopicService topicService) {
    super(ExternalMqttClient.class, UnsubscribeMqttInMessage.class, messageOutFactoryService);
    this.subscriptionService = subscriptionService;
    this.topicService = topicService;
  }

  @Override
  public MqttMessageType messageType() {
    return MqttMessageType.UNSUBSCRIBE;
  }

  @Override
  protected void processReceived(
      MqttConnection connection,
      ExternalMqttClient client,
      UnsubscribeMqttInMessage message) {

    Array<TopicFilter> topicFilters = message
        .rawTopicFilters()
        .stream()
        .map(rawTopicFilter -> topicService.createTopicFilter(client, rawTopicFilter))
        .collect(ArrayCollectors.toArray(TopicFilter.class));

    Array<UnsubscribeAckReasonCode> ackReasonCodes = subscriptionService
        .unsubscribe(client, topicFilters);

    client.send(messageOutFactoryService
        .resolveFactory(client)
        .newUnsubscribeAck(message.messageId(), ackReasonCodes));
  }
}
