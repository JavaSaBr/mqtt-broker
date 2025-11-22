package javasabr.mqtt.service.publish.handler.impl;

import javasabr.mqtt.model.MqttProperties;
import javasabr.mqtt.model.MqttProtocolErrors;
import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.model.reason.code.DisconnectReasonCode;
import javasabr.mqtt.network.impl.ExternalMqttClient;
import javasabr.mqtt.network.message.out.MqttOutMessage;
import javasabr.mqtt.network.session.MqttSession;
import javasabr.mqtt.service.MessageOutFactoryService;
import javasabr.mqtt.service.PublishDeliveringService;
import javasabr.mqtt.service.SubscriptionService;

public class Qos0MqttPublishInMessageHandler extends AbstractMqttPublishInMessageHandler<ExternalMqttClient> {

  public Qos0MqttPublishInMessageHandler(
      SubscriptionService subscriptionService,
      PublishDeliveringService publishDeliveringService,
      MessageOutFactoryService messageOutFactoryService) {
    super(ExternalMqttClient.class, subscriptionService, publishDeliveringService, messageOutFactoryService);
  }

  @Override
  public QoS qos() {
    return QoS.AT_MOST_ONCE;
  }

  @Override
  protected boolean validateImpl(ExternalMqttClient client, MqttSession session, Publish publish) {
    int messageId = publish.messageId();
    if (messageId != MqttProperties.MESSAGE_ID_IS_NOT_SET) {
      handleNotExpectedMessageId(client);
      return false;
    }
    return super.validateImpl(client, session, publish);
  }

  private void handleNotExpectedMessageId(ExternalMqttClient client) {
    MqttOutMessage response = messageOutFactoryService
        .resolveFactory(client)
        .newDisconnect(client, DisconnectReasonCode.PROTOCOL_ERROR, MqttProtocolErrors.NOT_EXPECTED_MESSAGE_ID);
    client.closeWithReason(response);
  }
}
