package javasabr.mqtt.network;

import javasabr.mqtt.model.subscriber.SubscribeTopicFilter;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.mqtt.network.packet.HasPacketId;
import javasabr.mqtt.network.packet.in.PublishInPacket;
import javasabr.rlib.functions.TriConsumer;

public interface MqttSession {

  interface UnsafeMqttSession extends MqttSession {

    void expirationTime(long expirationTime);

    void clear();

    void onPersisted();

    void onRestored();
  }

  interface PendingPacketHandler {

    /**
     * @return true if pending packet can be removed.
     */
    boolean handleResponse(MqttClient client, HasPacketId response);

    default void resend(MqttClient client, PublishInPacket packet, int packetId) {}
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

  void registerOutPublish(PublishInPacket publish, PendingPacketHandler handler, int packetId);

  void registerInPublish(PublishInPacket publish, PendingPacketHandler handler, int packetId);

  void updateOutPendingPacket(MqttClient client, HasPacketId response);

  void updateInPendingPacket(MqttClient client, HasPacketId response);

  <A, B> void forEachTopicFilter(
      A arg1,
      B arg2,
      TriConsumer<A, B, SubscribeTopicFilter> consumer);

  void addSubscriber(SubscribeTopicFilter subscribe);

  void removeSubscriber(TopicFilter subscribe);
}
