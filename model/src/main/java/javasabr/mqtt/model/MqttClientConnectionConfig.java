package javasabr.mqtt.model;

public record MqttClientConnectionConfig(
    QoS maxQos,
    MqttVersion mqttVersion,
    long sessionExpiryInterval,
    int receiveMaxPublishes,
    int maxPacketSize,
    int topicAliasMaxValue,
    int keepAlive,
    boolean requestResponseInformation,
    boolean requestProblemInformation,
    boolean sessionsEnabled,
    boolean retainAvailable,
    boolean wildcardSubscriptionAvailable,
    boolean subscriptionIdAvailable,
    boolean sharedSubscriptionAvailable) {}
