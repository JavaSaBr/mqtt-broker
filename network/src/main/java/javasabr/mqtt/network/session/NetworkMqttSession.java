package javasabr.mqtt.network.session;

import javasabr.mqtt.model.message.TrackableMqttMessage;
import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.model.session.MqttSession;
import javasabr.mqtt.network.user.NetworkMqttUser;

public interface NetworkMqttSession extends MqttSession {
  
  interface PendingMessageHandler {

    /**
     * @return true if pending packet can be removed.
     */
    boolean handleResponse(NetworkMqttUser user, TrackableMqttMessage response);

    default void resend(NetworkMqttUser user, Publish publish) {}
  }
  
  void resendPendingPackets(NetworkMqttUser user);

  void registerOutPublish(Publish publish, PendingMessageHandler handler);

  void updateOutPendingPacket(NetworkMqttUser user, TrackableMqttMessage response);
}
