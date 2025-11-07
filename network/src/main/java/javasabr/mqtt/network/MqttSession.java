package javasabr.mqtt.network;

import javasabr.mqtt.model.TrackableMessage;
import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.model.subscribtion.Subscription;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.rlib.collections.array.Array;

public interface MqttSession {

  interface UnsafeMqttSession extends MqttSession {

    void expirationTime(long expirationTime);

    void clear();

    void onPersisted();

    void onRestored();
  }

  interface PendingMessageHandler {

    /**
     * @return true if pending packet can be removed.
     */
    boolean handleResponse(MqttClient client, TrackableMessage response);

    default void resend(MqttClient client, Publish publish) {}
  }

  String clientId();

  int nextMessageId();

  /**
   * @return the expiration time in ms or -1 if it should not be expired now.
   */
  long expirationTime();

  void resendPendingPackets(MqttClient client);

  boolean hasOutPending();

  boolean hasInPending();

  boolean hasInPending(int messageId);

  boolean hasOutPending(int messageId);

  void registerOutPublish(Publish publish, PendingMessageHandler handler);

  void registerInPublish(Publish publish, PendingMessageHandler handler);

  void updateOutPendingPacket(MqttClient client, TrackableMessage response);

  void updateInPendingPacket(MqttClient client, TrackableMessage response);

  void storeSubscription(Subscription subscription);

  void removeSubscription(TopicFilter subscribe);

  void removeSubscription(Subscription subscription);

  Array<Subscription> storedSubscriptions();

  Array<Subscription> findStoredSubscriptionWithId(int subscriptionId);
}
