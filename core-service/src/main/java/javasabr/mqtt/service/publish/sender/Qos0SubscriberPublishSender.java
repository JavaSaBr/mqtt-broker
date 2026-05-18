package javasabr.mqtt.service.publish.sender;

import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.publish.IncomingPublish;
import javasabr.mqtt.model.publish.OutgoingPublish;
import javasabr.mqtt.model.publish.SimpleOutgoingPublish;
import javasabr.mqtt.model.session.MqttSession;
import javasabr.mqtt.network.impl.ExternalNetworkMqttUser;
import javasabr.mqtt.network.message.out.MqttOutMessage;
import javasabr.mqtt.service.MessageOutFactoryService;
import javasabr.mqtt.service.publish.IncomingPublishStorage;
import javasabr.rlib.collections.array.IntArray;
import lombok.CustomLog;
import org.jspecify.annotations.Nullable;

@CustomLog
public class Qos0SubscriberPublishSender extends AbstractSubscriberPublishSender<ExternalNetworkMqttUser> {

  public Qos0SubscriberPublishSender(
      MessageOutFactoryService messageOutFactoryService,
      IncomingPublishStorage incomingPublishStorage) {
    super(ExternalNetworkMqttUser.class, messageOutFactoryService, incomingPublishStorage);
  }

  @Override
  public QoS qos() {
    return QoS.AT_MOST_ONCE;
  }

  @Nullable
  @Override
  protected OutgoingPublish buildOutgoingPublish(
      ExternalNetworkMqttUser user,
      MqttSession session,
      IncomingPublish incomingPublish) {
    return new SimpleOutgoingPublish(incomingPublish, incomingPublish.retained(), IntArray.EMPTY);
  }

  @Override
  protected void send(
      ExternalNetworkMqttUser user,
      OutgoingPublish outgoingPublish,
      MqttOutMessage mqttOutMessage) {
    // for QoS 0 we don't need any confirmation from client side
    user
        .sendAsync(mqttOutMessage)
        .whenComplete((_, ex) -> {
          incomingPublishStorage.decreaseConsumerCount(outgoingPublish.source(), 1);
          if (ex != null) {
            log.error(ex);
          }
        });
  }
}
