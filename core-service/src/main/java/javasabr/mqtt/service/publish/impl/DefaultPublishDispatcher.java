package javasabr.mqtt.service.publish.impl;

import java.util.Collection;
import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.publish.IncomingPublish;
import javasabr.mqtt.model.subscription.Subscription;
import javasabr.mqtt.service.publish.PublishDispatcher;
import javasabr.mqtt.service.publish.sender.SubscriberPublishSender;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;

@CustomLog
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DefaultPublishDispatcher implements PublishDispatcher {

  @Nullable
  SubscriberPublishSender[] publishSenders;

  public DefaultPublishDispatcher(
      Collection<? extends SubscriberPublishSender> knownSubscriberPublishSenders) {

    int maxIndex = knownSubscriberPublishSenders
        .stream()
        .map(SubscriberPublishSender::qos)
        .mapToInt(QoS::level)
        .max()
        .orElse(0);

    var senders = new SubscriberPublishSender[maxIndex + 1];

    for (SubscriberPublishSender sender : knownSubscriberPublishSenders) {
      QoS qos = sender.qos();
      if (senders[qos.level()] != null) {
        throw new IllegalArgumentException(
            "Found duplicate SubscriberPublishSender:[" + sender + "]");
      }
      senders[qos.level()] = sender;
    }

    this.publishSenders = senders;
    log.info(senders, DefaultPublishDispatcher::buildServiceDescription);
  }

  @Override
  public void dispatchToSubscriber(IncomingPublish publish, MqttUser user, Subscription subscription) {
    try {
      //noinspection DataFlowIssue
      publishSenders[subscription.qos().level()].sendToSubscriber(publish, user);
    } catch (IndexOutOfBoundsException | NullPointerException ex) {
      log.warning(publish, "Received not supported publish message:[%s]"::formatted);
    }
  }

  private static String buildServiceDescription(@Nullable SubscriberPublishSender[] senders) {
    var builder = new StringBuilder();
    builder.append("{\n");
    int count = 0;
    for (SubscriberPublishSender sender : senders) {
      if (sender == null) {
        continue;
      }
      count++;
      builder
          .append("  \"")
          .append(sender.qos())
          .append("\": \"")
          .append(sender
              .getClass()
              .getSimpleName())
          .append("\",")
          .append("\n");
    }
    builder
        .delete(builder.length() - 2, builder.length())
        .append("\n}");

    return "Registered [%s] SubscriberPublishSenders: %s".formatted(count, builder);
  }
}
