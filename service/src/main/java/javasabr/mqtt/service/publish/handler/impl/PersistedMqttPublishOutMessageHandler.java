package javasabr.mqtt.service.publish.handler.impl;

import javasabr.mqtt.model.MqttProperties;
import javasabr.mqtt.model.TrackableMessage;
import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.network.MqttClient;
import javasabr.mqtt.network.impl.ExternalMqttClient;
import javasabr.mqtt.network.session.MqttSession;
import javasabr.mqtt.network.session.MqttSession.PendingMessageHandler;
import javasabr.mqtt.service.MessageOutFactoryService;
import javasabr.mqtt.service.SubscriptionService;
import javasabr.mqtt.service.publish.handler.PublishHandlingResult;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;

@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public abstract class PersistedMqttPublishOutMessageHandler extends
    AbstractMqttPublishOutMessageHandler<ExternalMqttClient> {

  PendingMessageHandler pendingMessageHandler;

  protected PersistedMqttPublishOutMessageHandler(
      SubscriptionService subscriptionService,
      MessageOutFactoryService messageOutFactoryService) {
    super(ExternalMqttClient.class, subscriptionService, messageOutFactoryService);
    this.pendingMessageHandler = new PendingMessageHandler() {
      @Override
      public boolean handleResponse(MqttClient client, TrackableMessage response) {
        return handleReceivedResponse(client, response);
      }
      @Override
      public void resend(MqttClient client, Publish publish) {
        tryToDeliverAgain(client, publish);
      }
    };
  }

  @Nullable
  @Override
  protected Publish reconstruct(MqttClient client, Publish original) {
    MqttSession session = client.session();
    if (session == null) {
      return null;
    }
    return original.with(
        // generate new uniq packet id per client
        session.nextMessageId(),
        qos(),
        false,
        MqttProperties.TOPIC_ALIAS_UNDEFINED);
  }

  @Override
  protected PublishHandlingResult handleImpl(Publish publish, ExternalMqttClient client) {

    MqttSession session = client.session();
    if (session == null) {
      return PublishHandlingResult.SKIPPED;
    }

    // register waiting async response
    session.registerOutPublish(publish, pendingMessageHandler);

    // send publish
    startDelivering(client, publish);
    return PublishHandlingResult.SUCCESS;
  }

  protected boolean handleReceivedResponse(MqttClient client, TrackableMessage response) {
    return false;
  }

  protected void tryToDeliverAgain(MqttClient client, Publish publish) {
    startDelivering(client, publish.withDuplicated());
  }
}
