package javasabr.mqtt.service.publish.handler.impl;

import javasabr.mqtt.model.MqttProperties;
import javasabr.mqtt.model.MqttProtocolErrors;
import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.model.reason.code.DisconnectReasonCode;
import javasabr.mqtt.model.session.MessageTacker;
import javasabr.mqtt.network.message.out.MqttOutMessage;
import javasabr.mqtt.network.session.NetworkMqttSession;
import javasabr.mqtt.network.user.NetworkMqttUser;
import javasabr.mqtt.service.MessageOutFactoryService;
import javasabr.mqtt.service.PublishDeliveringService;
import javasabr.mqtt.service.SubscriptionService;

public abstract class TrackableMqttPublishInMessageHandler<U extends NetworkMqttUser>
    extends AbstractMqttPublishInMessageHandler<U> {

  public TrackableMqttPublishInMessageHandler(
      Class<U> expectedClientType,
      SubscriptionService subscriptionService,
      PublishDeliveringService publishDeliveringService,
      MessageOutFactoryService messageOutFactoryService) {
    super(expectedClientType, subscriptionService, publishDeliveringService, messageOutFactoryService);
  }

  @Override
  protected boolean validateImpl(U user, NetworkMqttSession session, Publish publish) {
    int messagedId = publish.messageId();
    if (messagedId == MqttProperties.MESSAGE_ID_IS_NOT_SET) {
      handleMissedMessageId(user);
      return false;
    }
    return super.validateImpl(user, session, publish);
  }

  @Override
  protected void handleImpl(U user, NetworkMqttSession session, Publish publish) {
    MessageTacker messageTacker = session.inMessageTracker();
    messageTacker.add(publish.messageId(), MqttMessageType.PUBLISH);
    super.handleImpl(user, session, publish);
  }

  protected void handleMissedMessageId(U client) {
    MqttOutMessage response = messageOutFactoryService
        .resolveFactory(client)
        .newDisconnect(client, DisconnectReasonCode.PROTOCOL_ERROR, MqttProtocolErrors.MISSED_REQUIRED_MESSAGE_ID);
    client.closeWithReason(response);
  }
}
