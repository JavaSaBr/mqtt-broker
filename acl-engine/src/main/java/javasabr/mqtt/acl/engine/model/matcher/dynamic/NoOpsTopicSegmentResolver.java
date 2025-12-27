package javasabr.mqtt.acl.engine.model.matcher.dynamic;

import com.fasterxml.jackson.annotation.JsonValue;
import javasabr.mqtt.model.MqttUser;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class NoOpsTopicSegmentResolver extends TopicSegmentResolver {
  
  String segment;
  
  @Override
  public String resolve(MqttUser user) {
    return segment;
  }

  @Override
  public String toString() {
    return "AsIs[" + segment + "]";
  }

  @JsonValue
  Object jsonDebugValue() {
    return toString();
  }
}
