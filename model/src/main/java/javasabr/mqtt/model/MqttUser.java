package javasabr.mqtt.model;

import java.util.concurrent.CompletionStage;
import javasabr.mqtt.model.annotations.NotContainsChars;
import javasabr.mqtt.model.message.SendableMqttMessage;
import javasabr.mqtt.model.session.MqttSession;
import javasabr.mqtt.model.topic.TopicFilter;
import org.jspecify.annotations.Nullable;

public interface MqttUser {

  @NotContainsChars({
      TopicFilter.MULTI_LEVEL_WILDCARD_CHAR,
      TopicFilter.SINGLE_LEVEL_WILDCARD_CHAR,
      TopicFilter.DELIMITER_CHAR
  })
  String clientId();

  @NotContainsChars({
      TopicFilter.MULTI_LEVEL_WILDCARD_CHAR,
      TopicFilter.SINGLE_LEVEL_WILDCARD_CHAR,
      TopicFilter.DELIMITER_CHAR
  })
  @Nullable
  String userName();

  @NotContainsChars({
      TopicFilter.MULTI_LEVEL_WILDCARD_CHAR,
      TopicFilter.SINGLE_LEVEL_WILDCARD_CHAR,
      TopicFilter.DELIMITER_CHAR
  })
  String ipAddress();
  
  @Nullable 
  MqttSession session();

  MqttClientConnectionConfig connectionConfig();
  
  void sendInBackground(SendableMqttMessage message);

  /**
   * @return the feature with result of delivering the message
   */
  CompletionStage<Boolean> sendAsync(SendableMqttMessage message);
}
