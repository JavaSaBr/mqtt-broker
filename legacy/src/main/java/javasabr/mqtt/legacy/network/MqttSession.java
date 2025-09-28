package javasabr.mqtt.legacy.network;

import javasabr.mqtt.legacy.network.packet.HasPacketId;
import javasabr.mqtt.legacy.network.packet.in.PublishInPacket;
import javasabr.mqtt.model.subscriber.SubscribeTopicFilter;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.rlib.functions.TriConsumer;

public interface MqttSession {

  interface UnsafeMqttSession extends MqttSession {

    void setExpirationTime(long expirationTime);

    void clear();

    void onPersisted();

    void onRestored();
  }

  interface PendingPacketHandler {

    /**
     * @return true if pending packet can be removed.
     */
    boolean handleResponse(MqttClient client, HasPacketId response);

    default void resend(MqttClient client, PublishInPacket packet, int packetId) {
    }
  }

  String getClientId();

  int nextPacketId();

  /**
   * @return the expiration time in ms or -1 if it should not be expired now.
   */
  long getExpirationTime();

  void resendPendingPackets(MqttClient client);

  boolean hasOutPending();

  boolean hasInPending();

  boolean hasInPending(int packetId);

  boolean hasOutPending(int packetId);

  void registerOutPublish(PublishInPacket publish, PendingPacketHandler handler, int packetId);

  void registerInPublish(PublishInPacket publish, PendingPacketHandler handler, int packetId);

  void updateOutPendingPacket(MqttClient client, HasPacketId response);

  void updateInPendingPacket(MqttClient client, HasPacketId response);

  <F, S> void forEachTopicFilter(
      F first,
      S second,
      TriConsumer<F, S, SubscribeTopicFilter> consumer);

  void addSubscriber(SubscribeTopicFilter subscribe);

  void removeSubscriber(TopicFilter subscribe);
}
