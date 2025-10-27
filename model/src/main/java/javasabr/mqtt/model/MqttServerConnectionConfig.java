package javasabr.mqtt.model;

public record MqttServerConnectionConfig(
    QoS maxQos,
    int maxPacketSize,
    int maxStringLength,
    int maxBinarySize,
    int maxTopicLevels,
    int minKeepAliveTime,
    int receiveMaxPublishes,
    int topicAliasMaxValue,
    long defaultSessionExpiryInterval,
    boolean keepAliveEnabled,
    boolean sessionsEnabled,
    boolean retainAvailable,
    boolean wildcardSubscriptionAvailable,
    boolean subscriptionIdAvailable,
    boolean sharedSubscriptionAvailable) {
}
