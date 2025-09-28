package javasabr.mqtt.model.network.connection;

import javasabr.mqtt.model.QoS;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
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
