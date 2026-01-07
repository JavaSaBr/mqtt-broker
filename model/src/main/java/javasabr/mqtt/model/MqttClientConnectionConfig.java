package javasabr.mqtt.model;

import java.time.Duration;
import org.jspecify.annotations.Nullable;

public record MqttClientConnectionConfig(
    MqttServerConnectionConfig server,
    QoS maxQos,
    MqttVersion mqttVersion,
    Duration sessionExpiryInterval,
    int receiveMaxPublishes,
    int maxMessageSize,
    int topicAliasMaxValue,
    int keepAlive,
    boolean requestResponseInformation,
    boolean requestProblemInformation) {

  public boolean subscriptionIdAvailable() {
    return server.subscriptionIdAvailable();
  }

  public boolean retainAvailable() {
    return server.retainAvailable();
  }

  public boolean wildcardSubscriptionAvailable() {
    return server.wildcardSubscriptionAvailable();
  }

  public boolean sharedSubscriptionAvailable() {
    return server.sharedSubscriptionAvailable();
  }

  public boolean sessionsEnabled() {
    return server.sessionsEnabled();
  }

  public int maxTopicLevels() {
    return server.maxTopicLevels();
  }

  public int maxStringLength() {
    return server.maxStringLength();
  }

  public int maxBinarySize() {
    return server.maxBinarySize();
  }

  public long sessionExpiryIntervalInSecs() {
    return sessionExpiryInterval.toSeconds();
  }
}
