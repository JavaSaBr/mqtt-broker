package javasabr.mqtt.service.publish.impl;

import java.util.Collection;
import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.publish.Publish;
import javasabr.mqtt.network.user.NetworkMqttUser;
import javasabr.mqtt.service.publish.IncomingPublishRouter;
import javasabr.mqtt.service.publish.processor.IncomingPublishProcessor;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;

@CustomLog
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DefaultIncomingPublishRouter implements IncomingPublishRouter {

  @Nullable
  IncomingPublishProcessor[] incomingPublishProcessors;

  public DefaultIncomingPublishRouter(
      Collection<? extends IncomingPublishProcessor> knownIncomingPublishProcessors) {

    int maxIndex = knownIncomingPublishProcessors
        .stream()
        .map(IncomingPublishProcessor::qos)
        .mapToInt(QoS::level)
        .max()
        .orElse(0);

    var processors = new IncomingPublishProcessor[maxIndex + 1];

    for (IncomingPublishProcessor processor : knownIncomingPublishProcessors) {
      QoS qos = processor.qos();
      if (processors[qos.level()] != null) {
        throw new IllegalArgumentException(
            "Found duplicate IncomingPublishProcessor:[" + processor + "]");
      }
      processors[qos.level()] = processor;
    }

    this.incomingPublishProcessors = processors;
    log.info(processors, DefaultIncomingPublishRouter::buildServiceDescription);
  }
  
  @Override
  public void route(NetworkMqttUser user, Publish publish) {
    log.debug(user.clientId(), publish, "[%s] Start processing publish:%s"::formatted);
    QoS qos = publish.qos();
    try {
      //noinspection DataFlowIssue
      incomingPublishProcessors[qos.level()].process(user, publish);
    } catch (IndexOutOfBoundsException | NullPointerException ex) {
      log.warning(user.clientId(), publish, "[%s] Received not supported publish:%s"::formatted);
    }
  }
  
  private static String buildServiceDescription(@Nullable IncomingPublishProcessor[] processors) {
    var builder = new StringBuilder();
    builder.append("{\n");
    int count = 0;
    for (IncomingPublishProcessor processor : processors) {
      if (processor == null) {
        continue;
      }
      count++;
      builder
          .append("  \"")
          .append(processor.qos())
          .append("\": \"")
          .append(processor
              .getClass()
              .getSimpleName())
          .append("\",")
          .append("\n");
    }
    builder
        .delete(builder.length() - 2, builder.length())
        .append("\n}");

    return "Registered [%s] IncomingPublishProcessors: %s".formatted(count, builder);
  }
}
