package javasabr.mqtt.model.utils;

import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.subscriber.SharedSubscriber;
import javasabr.mqtt.model.subscriber.SingleSubscriber;
import javasabr.mqtt.model.subscriber.Subscriber;
import org.jspecify.annotations.Nullable;

public class SubscriberUtils {

  private static boolean isSharedSubscriber(Subscriber subscriber) {
    return subscriber instanceof SharedSubscriber;
  }

  private static boolean isSingleSubscriber(Subscriber subscriber) {
    return subscriber instanceof SingleSubscriber;
  }

  public static boolean isSharedSubscriberWithGroup(String group, Subscriber subscriber) {
    return isSharedSubscriber(subscriber) && group.equals(((SharedSubscriber) subscriber).getGroup());
  }

  @Nullable
  public static MqttUser singleSubscriberToMqttUser(Subscriber subscriber) {
    return isSingleSubscriber(subscriber) ? ((SingleSubscriber) subscriber).getUser() : null;
  }
}
