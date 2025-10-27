package javasabr.mqtt.model.subscriber;

import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.subscribtion.Subscription;
import javasabr.mqtt.model.subscribtion.SubscriptionOwner;

public record SingleSubscriber(SubscriptionOwner owner, Subscription subscription) implements Subscriber {

  @Override
  public SingleSubscriber resolveSingle() {
    return this;
  }

  public QoS qos() {
    return subscription.qos();
  }
}
