package javasabr.mqtt.network.session;

import javasabr.mqtt.model.message.TrackableMqttMessage;
import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.model.session.MqttSession;
import javasabr.mqtt.network.MqttClient;

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
    boolean handleResponse(MqttClient client, TrackableMqttMessage response);

    default void resend(MqttClient client, Publish publish) {}
  }
  
  void resendPendingPackets(MqttClient client);
  
  boolean hasOutPending();
  
  boolean hasOutPending(int messageId);

  void registerOutPublish(Publish publish, PendingMessageHandler handler);

  void updateOutPendingPacket(MqttClient client, TrackableMqttMessage response);
}
