package javasabr.mqtt.service.impl;

import java.util.Collection;
import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.network.MqttClient;
import javasabr.mqtt.service.PublishReceivingService;
import javasabr.mqtt.service.publish.handler.MqttPublishInMessageHandler;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;

@CustomLog
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DefaultPublishReceivingService implements PublishReceivingService {

  @Nullable
  MqttPublishInMessageHandler[] publishInHandlers;

  public DefaultPublishReceivingService(
      Collection<? extends MqttPublishInMessageHandler> knownPublishInHandlers) {

    int maxIndex = knownPublishInHandlers
        .stream()
        .map(MqttPublishInMessageHandler::qos)
        .mapToInt(QoS::level)
        .max()
        .orElse(0);

    var handlers = new MqttPublishInMessageHandler[maxIndex + 1];

    for (MqttPublishInMessageHandler knownPublishInHandler : knownPublishInHandlers) {
      QoS qos = knownPublishInHandler.qos();
      if (handlers[qos.level()] != null) {
        throw new IllegalArgumentException(
            "Found duplicate MqttPublishInMessageHandler:[" + knownPublishInHandler + "]");
      }
      handlers[qos.level()] = knownPublishInHandler;
    }

    this.publishInHandlers = handlers;
    log.info(publishInHandlers, DefaultPublishReceivingService::buildServiceDescription);
  }

  @Override
  public void processPublish(MqttClient client, Publish publish) {
    log.debug(client.clientId(), publish, "[%s] Start processing publish:%s"::formatted);
    QoS qos = publish.qos();
    try {
      //noinspection DataFlowIssue
      publishInHandlers[qos.level()].handle(client, publish);
    } catch (IndexOutOfBoundsException | NullPointerException ex) {
      log.warning(client.clientId(), publish, "[%s] Received not supported publish:%s"::formatted);
    }
  }


  private static String buildServiceDescription(
      @Nullable MqttPublishInMessageHandler[] publishInMessageHandlers) {
    var builder = new StringBuilder();
    builder.append("{\n");
    int count = 0;
    for (MqttPublishInMessageHandler publishInMessageHandler : publishInMessageHandlers) {
      if (publishInMessageHandler == null) {
        continue;
      }
      count++;
      builder
          .append("  \"")
          .append(publishInMessageHandler.qos())
          .append("\": \"")
          .append(publishInMessageHandler
              .getClass()
              .getSimpleName())
          .append("\",")
          .append("\n");
    }
    builder
        .delete(builder.length() - 2, builder.length())
        .append("\n}");

    return "Registered [%s] MqttPublishInMessageHandlers: %s".formatted(count, builder);
  }
}
