package javasabr.mqtt.model.subscriber;

import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.QoS;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode(of = "user")
@RequiredArgsConstructor
public final class SingleSubscriber implements Subscriber {

  @Getter
  private final MqttUser user;
  private final SubscribeTopicFilter subscribe;

  public QoS getQos() {
    return subscribe.getQos();
  }
}
