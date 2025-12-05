package javasabr.mqtt.model;

import javasabr.rlib.common.util.NumberUtils;
import lombok.Builder;

@Builder(toBuilder = true)
public record MqttServerConnectionConfig(
    QoS maxQos,
    int maxMessageSize,
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

  public MqttServerConnectionConfig(
      QoS maxQos,
      int maxMessageSize,
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
    this.maxQos = maxQos;
    this.maxMessageSize = NumberUtils.validate(
        maxMessageSize,
        MqttProperties.MAXIMUM_MESSAGE_SIZE_MIN,
        MqttProperties.MAXIMUM_MESSAGE_SIZE_MAX);
    this.maxStringLength = NumberUtils.validate(
        maxStringLength,
        1,
        maxMessageSize / 2);
    this.maxBinarySize = NumberUtils.validate(
        maxBinarySize,
        1,
        maxMessageSize);
    this.maxTopicLevels = NumberUtils.validate(
        maxTopicLevels,
        1,
        Byte.MAX_VALUE);
    this.minKeepAliveTime = NumberUtils.validate(
        minKeepAliveTime,
        MqttProperties.SERVER_KEEP_ALIVE_MIN,
        MqttProperties.SERVER_KEEP_ALIVE_MAX);
    this.receiveMaxPublishes = receiveMaxPublishes;
    this.topicAliasMaxValue = NumberUtils.validate(
        topicAliasMaxValue,
        MqttProperties.TOPIC_ALIAS_MAXIMUM_DISABLED,
        MqttProperties.TOPIC_ALIAS_MAX);
    this.defaultSessionExpiryInterval = defaultSessionExpiryInterval;
    this.keepAliveEnabled = keepAliveEnabled;
    this.sessionsEnabled = sessionsEnabled;
    this.retainAvailable = retainAvailable;
    this.wildcardSubscriptionAvailable = wildcardSubscriptionAvailable;
    this.subscriptionIdAvailable = subscriptionIdAvailable;
    this.sharedSubscriptionAvailable = sharedSubscriptionAvailable;
  }

  public MqttServerConnectionConfig withMaxQos(QoS maxQos) {
    return toBuilder()
        .maxQos(maxQos)
        .build();
  }

  public MqttServerConnectionConfig withWildcardSubscriptionAvailable(boolean wildcardSubscriptionAvailable) {
    return toBuilder()
        .wildcardSubscriptionAvailable(wildcardSubscriptionAvailable)
        .build();
  }

  public MqttServerConnectionConfig withSharedSubscriptionAvailable(boolean sharedSubscriptionAvailable) {
    return toBuilder()
        .sharedSubscriptionAvailable(sharedSubscriptionAvailable)
        .build();
  }

  public MqttServerConnectionConfig withSubscriptionIdAvailable(boolean subscriptionIdAvailable) {
    return toBuilder()
        .subscriptionIdAvailable(subscriptionIdAvailable)
        .build();
  }

  public MqttServerConnectionConfig withRetainAvailable(boolean retainAvailable) {
    return toBuilder()
        .retainAvailable(retainAvailable)
        .build();
  }
}
