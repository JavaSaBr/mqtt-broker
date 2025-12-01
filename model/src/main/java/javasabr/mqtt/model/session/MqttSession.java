package javasabr.mqtt.model.session;

public interface MqttSession {
  
  String clientId();

  int generateMessageId();

  /**
   * @return the expiration time in ms or -1 if it should not be expired now.
   */
  long expirationTime();
  
  MessageTacker inMessageTracker();
  MessageTacker outMessageTracker();

  ProcessingPublishes inProcessingPublishes();
  ProcessingPublishes outProcessingPublishes();

  ActiveSubscriptions activeSubscriptions();
  TopicNameMapping topicNameMapping();
  
}
