package javasabr.mqtt.acl.engine.model.matcher.dynamic;

import com.fasterxml.jackson.annotation.JsonValue;
import javasabr.mqtt.model.MqttUser;
import org.jspecify.annotations.Nullable;

public class ClientIdTopicSegmentResolver extends TopicSegmentResolver {

  public static final String VARIABLE = "{clientId}";
  
  @Nullable
  @Override
  public String resolve(MqttUser user) {
    return user.clientId();
  }
  
  @Override
  public String toString() {
    return VARIABLE;
  }

  @JsonValue
  Object jsonDebugValue() {
    return toString();
  }
}
