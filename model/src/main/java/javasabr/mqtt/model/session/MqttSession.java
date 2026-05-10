package javasabr.mqtt.model.session;

import java.time.Duration;
import java.util.UUID;
import javasabr.mqtt.model.publish.IncomingPublish;
import javasabr.mqtt.model.publish.OutgoingPublish;
import org.jspecify.annotations.Nullable;

public interface MqttSession {
  
  String clientId();

  int generateMessageId();
  UUID generateDataId();
  UUID generatePublishId();
  
  @Nullable
  Duration expiryInterval();
  
  MessageTacker inMessageTracker();
  MessageTacker outMessageTracker();

  ProcessingPublishes<IncomingPublish> incomingProcessingPublishes();
  ProcessingPublishes<OutgoingPublish> outgoingProcessingPublishes();

  ActiveSubscriptions activeSubscriptions();
  TopicNameMapping topicNameMapping();
}
