package javasabr.mqtt.service.impl;

import java.util.Collection;
import java.util.function.Function;
import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.model.subscriber.SingleSubscriber;
import javasabr.mqtt.model.subscription.Subscription;
import javasabr.mqtt.model.topic.tree.ConcurrentRetainedMessageTree;
import javasabr.mqtt.service.PublishDeliveringService;
import javasabr.mqtt.service.publish.handler.MqttPublishOutMessageHandler;
import javasabr.mqtt.service.publish.handler.PublishHandlingResult;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.collections.array.MutableArray;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;

@CustomLog
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DefaultPublishDeliveringService implements PublishDeliveringService {

  @Nullable
  MqttPublishOutMessageHandler[] publishOutMessageHandlers;
  ConcurrentRetainedMessageTree retainedMessageTree;

  public DefaultPublishDeliveringService(
      Collection<? extends MqttPublishOutMessageHandler> knownPublishOutHandlers) {

    int maxIndex = knownPublishOutHandlers
        .stream()
        .map(MqttPublishOutMessageHandler::qos)
        .mapToInt(QoS::level)
        .max()
        .orElse(0);

    var handlers = new MqttPublishOutMessageHandler[maxIndex + 1];

    for (MqttPublishOutMessageHandler knownPublishOutHandler : knownPublishOutHandlers) {
      QoS qos = knownPublishOutHandler.qos();
      if (handlers[qos.level()] != null) {
        throw new IllegalArgumentException(
            "Found duplicate MqttPublishOutMessageHandler:[" + knownPublishOutHandler + "]");
      }
      handlers[qos.level()] = knownPublishOutHandler;
    }
    this.retainedMessageTree = new ConcurrentRetainedMessageTree();
    this.publishOutMessageHandlers = handlers;
    log.info(publishOutMessageHandlers, DefaultPublishDeliveringService::buildServiceDescription);
  }

  @Override
  public PublishHandlingResult startDelivering(Publish publish, SingleSubscriber subscriber) {
    if (publish.retained()) {
      Subscription subscription = subscriber.subscription();
      boolean retainAsPublished = subscription.retainAsPublished();
      Function<Publish, Publish> transformer = retainAsPublished ? Function.identity() : Publish::withoutRetain;
      retainedMessageTree.retainMessage(transformer.apply(publish));
    }
    return startDeliveringWithoutRetain(publish, subscriber);
  }

  @Override
  public Array<PublishHandlingResult> deliverRetainedMessages(SingleSubscriber subscriber) {
    Subscription subscription = subscriber.subscription();
    Array<Publish> retainedMessages = retainedMessageTree.getRetainedMessage(subscription.topicFilter());
    MutableArray<PublishHandlingResult> result = MutableArray.ofType(PublishHandlingResult.class);
    for (Publish message : retainedMessages) {
      result.add(startDeliveringWithoutRetain(message, subscriber));
    }
    return Array.copyOf(result);
  }

  private PublishHandlingResult startDeliveringWithoutRetain(Publish publish, SingleSubscriber subscriber) {
    try {
      //noinspection DataFlowIssue
      return publishOutMessageHandlers[subscriber.qos().level()].handle(publish, subscriber);
    } catch (IndexOutOfBoundsException | NullPointerException ex) {
      log.warning(publish, "Received not supported publish message:[%s]"::formatted);
      return PublishHandlingResult.UNSPECIFIED_ERROR;
    }
  }

  private static String buildServiceDescription(
      @Nullable MqttPublishOutMessageHandler[] publishOutMessageHandlers) {
    var builder = new StringBuilder();
    builder.append("{\n");
    int count = 0;
    for (MqttPublishOutMessageHandler publishOutMessageHandler : publishOutMessageHandlers) {
      if (publishOutMessageHandler == null) {
        continue;
      }
      count++;
      builder
          .append("  \"")
          .append(publishOutMessageHandler.qos())
          .append("\": \"")
          .append(publishOutMessageHandler
              .getClass()
              .getSimpleName())
          .append("\",")
          .append("\n");
    }
    builder
        .delete(builder.length() - 2, builder.length())
        .append("\n}");

    return "Registered [%s] MqttPublishOutMessageHandlers: %s".formatted(count, builder);
  }
}
