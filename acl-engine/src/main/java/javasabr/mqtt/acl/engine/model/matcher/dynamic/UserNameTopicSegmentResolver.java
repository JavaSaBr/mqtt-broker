package javasabr.mqtt.acl.engine.model.matcher.dynamic;

import javasabr.mqtt.model.MqttUser;
import org.jspecify.annotations.Nullable;

public class UserNameTopicSegmentResolver extends TopicSegmentResolver {
  
  public static final String VARIABLE = "{userName}";
  
  @Nullable
  @Override
  public String resolve(MqttUser user) {
    return user.userName();
  }
}
