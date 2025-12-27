package javasabr.mqtt.acl.engine.model.matcher.dynamic;

import com.fasterxml.jackson.annotation.JsonValue;
import javasabr.mqtt.model.MqttUser;

public final class ClientIdTopicSegmentResolver extends TopicSegmentResolver {

  public static final String VARIABLE = "{clientId}";
  
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
