package javasabr.mqtt.legacy.util;

import javasabr.mqtt.legacy.model.SharedSubscriber;
import javasabr.mqtt.legacy.model.SingleSubscriber;
import javasabr.mqtt.legacy.model.Subscriber;
import javasabr.mqtt.legacy.network.client.MqttClient;
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
  public static MqttClient singleSubscriberToMqttClient(Subscriber subscriber) {
    return isSingleSubscriber(subscriber) ? ((SingleSubscriber) subscriber).getMqttClient() : null;
  }
}
