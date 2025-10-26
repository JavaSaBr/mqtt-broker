package javasabr.mqtt.network;

import javasabr.mqtt.model.subscriber.SubscribeTopicFilter;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.mqtt.network.message.HasMessageId;
import javasabr.mqtt.network.message.in.PublishMqttInMessage;
import javasabr.rlib.functions.TriConsumer;

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
    boolean handleResponse(MqttClient client, HasMessageId response);

    default void resend(MqttClient client, PublishMqttInMessage packet, int packetId) {}
  }

  String clientId();

  int nextPacketId();

  /**
   * @return the expiration time in ms or -1 if it should not be expired now.
   */
  long expirationTime();

  void resendPendingPackets(MqttClient client);

  boolean hasOutPending();

  boolean hasInPending();

  boolean hasInPending(int packetId);

  boolean hasOutPending(int packetId);

  void registerOutPublish(PublishMqttInMessage publish, PendingMessageHandler handler, int packetId);

  void registerInPublish(PublishMqttInMessage publish, PendingMessageHandler handler, int packetId);

  void updateOutPendingPacket(MqttClient client, HasMessageId response);

  void updateInPendingPacket(MqttClient client, HasMessageId response);

  <A, B> void forEachTopicFilter(
      A arg1,
      B arg2,
      TriConsumer<A, B, SubscribeTopicFilter> consumer);

  void addSubscriber(SubscribeTopicFilter subscribe);

  void removeSubscriber(TopicFilter subscribe);
}
