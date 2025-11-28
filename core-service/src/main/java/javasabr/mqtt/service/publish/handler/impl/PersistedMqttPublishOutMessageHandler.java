package javasabr.mqtt.service.publish.handler.impl;

import javasabr.mqtt.model.MqttProperties;
import javasabr.mqtt.model.message.TrackableMqttMessage;
import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.network.MqttNetworkSession;
import javasabr.mqtt.network.MqttNetworkSession.PendingMessageHandler;
import javasabr.mqtt.network.impl.ExternalNetworkMqttUser;
import javasabr.mqtt.network.user.NetworkMqttUser;
import javasabr.mqtt.service.MessageOutFactoryService;
import javasabr.mqtt.service.SubscriptionService;
import javasabr.mqtt.service.publish.handler.PublishHandlingResult;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;

@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public abstract class PersistedMqttPublishOutMessageHandler extends
    AbstractMqttPublishOutMessageHandler<ExternalNetworkMqttUser> {

  PendingMessageHandler pendingMessageHandler;

  protected PersistedMqttPublishOutMessageHandler(
      SubscriptionService subscriptionService,
      MessageOutFactoryService messageOutFactoryService) {
    super(ExternalNetworkMqttUser.class, subscriptionService, messageOutFactoryService);
    this.pendingMessageHandler = new PendingMessageHandler() {
      @Override
      public boolean handleResponse(NetworkMqttUser user, TrackableMqttMessage response) {
        return handleReceivedResponse(user, response);
      }
      @Override
      public void resend(NetworkMqttUser user, Publish publish) {
        tryToDeliverAgain(user, publish);
      }
    };
  }

  @Nullable
  @Override
  protected Publish reconstruct(NetworkMqttUser user, Publish original) {
    MqttNetworkSession session = user.session();
    if (session == null) {
      return null;
    }
    return original.with(
        // generate new uniq packet id per client
        session.generateMessageId(),
        qos(),
        false,
        MqttProperties.TOPIC_ALIAS_NOT_SET);
  }

  @Override
  protected PublishHandlingResult handleImpl(Publish publish, ExternalNetworkMqttUser client) {

    MqttNetworkSession session = client.session();
    if (session == null) {
      return PublishHandlingResult.SKIPPED;
    }

    // register waiting async response
    session.registerOutPublish(publish, pendingMessageHandler);

    // send publish
    startDelivering(client, publish);
    return PublishHandlingResult.SUCCESS;
  }

  protected boolean handleReceivedResponse(NetworkMqttUser user, TrackableMqttMessage response) {
    return false;
  }

  protected void tryToDeliverAgain(NetworkMqttUser client, Publish publish) {
    startDelivering(client, publish.withDuplicated());
  }
}
