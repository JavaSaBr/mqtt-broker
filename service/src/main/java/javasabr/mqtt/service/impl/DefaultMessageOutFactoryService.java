package javasabr.mqtt.service.impl;

import java.util.Collection;
import javasabr.mqtt.model.MqttClientConnectionConfig;
import javasabr.mqtt.model.MqttVersion;
import javasabr.mqtt.network.MqttClient;
import javasabr.mqtt.network.MqttClient.UnsafeMqttClient;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.service.MessageOutFactoryService;
import javasabr.mqtt.service.message.out.factory.MessageOutFactory;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;

@CustomLog
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DefaultMessageOutFactoryService implements MessageOutFactoryService {

  @Nullable
  MessageOutFactory[] factories;

  public DefaultMessageOutFactoryService(Collection<? extends MessageOutFactory> knownFactories) {

    int maxVersion = knownFactories
        .stream()
        .map(MessageOutFactory::mqttVersion)
        .mapToInt(MqttVersion::version)
        .max()
        .orElse(0);

    var factories = new MessageOutFactory[maxVersion + 1];

    for (MessageOutFactory knownFactory : knownFactories) {
      MqttVersion version = knownFactory.mqttVersion();
      if (factories[version.version()] != null) {
        throw new IllegalArgumentException("Found duplicate MqttMessageOutFactory:[" + knownFactory + "]");
      }
      factories[version.version()] = knownFactory;
    }

    this.factories = factories;
  }

  @Override
  public MessageOutFactory resolveFactory(MqttClient client) {
    if (client instanceof UnsafeMqttClient unsafe) {
      return resolveFactory(unsafe.connection());
    }
    throw new IllegalArgumentException("Unsupported client: " + client);
  }

  @Override
  public MessageOutFactory resolveFactory(MqttConnection connection) {
    MqttClientConnectionConfig connectionConfig = connection.clientConnectionConfig();
    MqttVersion mqttVersion = connectionConfig.mqttVersion();
    try {
      //noinspection DataFlowIssue
      return factories[mqttVersion.version()];
    } catch (IndexOutOfBoundsException | NullPointerException ex) {
      log.warning(mqttVersion, "Received not supported mqtt version:[%s]"::formatted);
      throw new IllegalArgumentException("Unsupported MQTT version:[" + mqttVersion + "]");
    }
  }
}
