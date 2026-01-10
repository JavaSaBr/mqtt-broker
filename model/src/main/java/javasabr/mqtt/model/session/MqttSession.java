package javasabr.mqtt.model.session;

import java.time.Duration;
import org.jspecify.annotations.Nullable;

public interface MqttSession {
  
  String clientId();

  int generateMessageId();
  
  @Nullable
  Duration expiryInterval();
  
  MessageTacker inMessageTracker();
  MessageTacker outMessageTracker();

  ProcessingPublishes inProcessingPublishes();
  ProcessingPublishes outProcessingPublishes();

  ActiveSubscriptions activeSubscriptions();
  TopicNameMapping topicNameMapping();
}
