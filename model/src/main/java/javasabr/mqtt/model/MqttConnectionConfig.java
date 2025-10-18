package javasabr.mqtt.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@RequiredArgsConstructor
@Accessors(fluent = true, chain = false)
public class MqttConnectionConfig {

  private final QoS maxQos;

  private final int maximumPacketSize;
  private final int minKeepAliveTime;
  private final int receiveMaximum;
  private final int topicAliasMaximum;

  private final long defaultSessionExpiryInterval;

  private final boolean keepAliveEnabled;
  private final boolean sessionsEnabled;
  private final boolean retainAvailable;
  private final boolean wildcardSubscriptionAvailable;
  private final boolean subscriptionIdAvailable;
  private final boolean sharedSubscriptionAvailable;
}
