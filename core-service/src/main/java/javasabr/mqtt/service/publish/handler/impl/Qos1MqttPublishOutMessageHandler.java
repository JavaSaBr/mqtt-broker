package javasabr.mqtt.service.publish.handler.impl;

import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.message.TrackableMqttMessage;
import javasabr.mqtt.network.message.in.PublishAckMqttInMessage;
import javasabr.mqtt.network.user.NetworkMqttUser;
import javasabr.mqtt.service.MessageOutFactoryService;
import javasabr.mqtt.service.SubscriptionService;

public class Qos1MqttPublishOutMessageHandler extends PersistedMqttPublishOutMessageHandler {

  public Qos1MqttPublishOutMessageHandler(
      SubscriptionService subscriptionService,
      MessageOutFactoryService messageOutFactoryService) {
    super(subscriptionService, messageOutFactoryService);
  }

  @Override
  public QoS qos() {
    return QoS.AT_LEAST_ONCE;
  }

  @Override
  protected boolean handleReceivedResponse(NetworkMqttUser user, TrackableMqttMessage response) {
    if (!(response instanceof PublishAckMqttInMessage)) {
      throw new IllegalStateException("Unexpected response: " + response);
    }
    // just return 'true' to remove pending packet from session
    return true;
  }
}
