package javasabr.mqtt.model.subscriber;

import com.fasterxml.jackson.annotation.JsonValue;
import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.subscription.Subscription;

public record SingleSubscriber(MqttUser user, Subscription subscription) implements Subscriber {

  @Override
  public SingleSubscriber resolveSingle() {
    return this;
  }

  public QoS qos() {
    return subscription.qos();
  }

  @JsonValue
  @Override
  public String toString() {
    return "[" + user + "]->[" + subscription.topicFilter().rawTopic() + "|" + subscription.qos().level() + "]";
  }
}
