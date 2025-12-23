package javasabr.mqtt.acl.engine.model.matcher.dynamic;

import javasabr.mqtt.model.MqttUser;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NoOpsTopicSegmentResolver extends TopicSegmentResolver {
  
  String segment;
  
  @Override
  public String resolve(MqttUser user) {
    return segment;
  }
}
