package javasabr.mqtt.model;

public record MqttClientConnectionConfig(
    MqttServerConnectionConfig server,
    QoS maxQos,
    MqttVersion mqttVersion,
    long sessionExpiryInterval,
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
}
