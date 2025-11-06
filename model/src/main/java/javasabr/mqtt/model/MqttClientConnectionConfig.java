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
    boolean requestProblemInformation) {}
