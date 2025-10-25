package javasabr.mqtt.service.publish.handler.impl;

import static javasabr.mqtt.model.reason.code.PublishReleaseReasonCode.SUCCESS;

import javasabr.mqtt.model.QoS;
import javasabr.mqtt.network.MqttClient;
import javasabr.mqtt.network.message.HasMessageId;
import javasabr.mqtt.network.message.in.PublishCompleteMqttInMessage;
import javasabr.mqtt.network.message.in.PublishReceivedMqttInMessage;
import javasabr.mqtt.service.MessageOutFactoryService;
import javasabr.mqtt.service.SubscriptionService;

public class Qos2MqttPublishOutMessageHandler extends PersistedMqttPublishOutMessageHandler {

  public Qos2MqttPublishOutMessageHandler(
      SubscriptionService subscriptionService,
      MessageOutFactoryService messageOutFactoryService) {
    super(subscriptionService, messageOutFactoryService);
  }

  @Override
  public QoS qos() {
    return QoS.EXACTLY_ONCE;
  }

  @Override
  protected boolean handleReceivedResponse(MqttClient client, HasMessageId response) {
    if (response instanceof PublishReceivedMqttInMessage) {
      client.send(messageOutFactoryService
          .resolveFactory(client)
          .newPublishRelease(response.messageId(), SUCCESS));
      return false;
    } else if (response instanceof PublishCompleteMqttInMessage) {
      return true;
    } else {
      throw new IllegalStateException("Unexpected response: " + response);
    }
  }
}
