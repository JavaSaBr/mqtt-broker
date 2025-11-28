package javasabr.mqtt.network;

import javasabr.mqtt.model.message.TrackableMqttMessage;
import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.model.session.MqttSession;
import javasabr.mqtt.network.user.NetworkMqttUser;

public interface MqttNetworkSession extends MqttSession {

  interface UnsafeMqttNetworkSession extends MqttNetworkSession {

    void expirationTime(long expirationTime);

    void clear();

    void onPersisted();

    void onRestored();
  }

  interface PendingMessageHandler {

    /**
     * @return true if pending packet can be removed.
     */
    boolean handleResponse(NetworkMqttUser user, TrackableMqttMessage response);

    default void resend(NetworkMqttUser user, Publish publish) {}
  }
  
  void resendPendingPackets(NetworkMqttUser client);
  
  boolean hasOutPending();
  
  boolean hasOutPending(int messageId);

  void registerOutPublish(Publish publish, PendingMessageHandler handler);

  void updateOutPendingPacket(NetworkMqttUser client, TrackableMqttMessage response);
}
