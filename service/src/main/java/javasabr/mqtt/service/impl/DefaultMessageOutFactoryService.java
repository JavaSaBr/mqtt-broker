package javasabr.mqtt.service.impl;

import java.util.Collection;
import javasabr.mqtt.model.MqttClientConnectionConfig;
import javasabr.mqtt.model.MqttVersion;
import javasabr.mqtt.network.MqttClient;
import javasabr.mqtt.network.MqttClient.UnsafeMqttClient;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.service.MessageOutFactoryService;
import javasabr.mqtt.service.message.out.factory.MqttMessageOutFactory;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;

@CustomLog
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DefaultMessageOutFactoryService implements MessageOutFactoryService {

  @Nullable
  MqttMessageOutFactory[] messageOutFactories;

  public DefaultMessageOutFactoryService(Collection<? extends MqttMessageOutFactory> knownFactories) {

    int maxVersion = knownFactories
        .stream()
        .map(MqttMessageOutFactory::mqttVersion)
        .mapToInt(MqttVersion::version)
        .max()
        .orElse(0);

    var factories = new MqttMessageOutFactory[maxVersion + 1];

    for (MqttMessageOutFactory knownFactory : knownFactories) {
      MqttVersion version = knownFactory.mqttVersion();
      if (factories[version.version()] != null) {
        throw new IllegalArgumentException("Found duplicate MessageOutFactory:[" + knownFactory + "]");
      }
      factories[version.version()] = knownFactory;
    }

    this.messageOutFactories = factories;
    log.info(messageOutFactories, DefaultMessageOutFactoryService::buildServiceDescription);
  }

  @Override
  public MqttMessageOutFactory resolveFactory(MqttClient client) {
    if (client instanceof UnsafeMqttClient unsafe) {
      return resolveFactory(unsafe.connection());
    }
    throw new IllegalArgumentException("Unsupported client: " + client);
  }

  @Override
  public MqttMessageOutFactory resolveFactory(MqttConnection connection) {
    MqttClientConnectionConfig connectionConfig = connection.clientConnectionConfig();
    MqttVersion mqttVersion = connectionConfig.mqttVersion();
    try {
      //noinspection DataFlowIssue
      return messageOutFactories[mqttVersion.version()];
    } catch (IndexOutOfBoundsException | NullPointerException ex) {
      log.warning(mqttVersion, "Received not supported mqtt version:[%s]"::formatted);
      throw new IllegalArgumentException("Unsupported MQTT version:[" + mqttVersion + "]");
    }
  }

  private static String buildServiceDescription(@Nullable MqttMessageOutFactory[] messageOutFactories) {
    var builder = new StringBuilder();
    builder.append("{\n");
    int count = 0;
    for (MqttMessageOutFactory mqttMessageOutFactory : messageOutFactories) {
      if (mqttMessageOutFactory == null) {
        continue;
      }
      count++;
      builder
          .append("  \"")
          .append(mqttMessageOutFactory.mqttVersion())
          .append("\": \"")
          .append(mqttMessageOutFactory
              .getClass()
              .getSimpleName())
          .append("\",")
          .append("\n");
    }
    builder
        .delete(builder.length() - 2, builder.length())
        .append("\n}");

    return "Registered [%s] MessageOutFactories: %s".formatted(count, builder);
  }
}
