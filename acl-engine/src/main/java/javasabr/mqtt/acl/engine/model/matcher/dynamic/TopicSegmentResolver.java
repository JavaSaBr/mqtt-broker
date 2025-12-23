package javasabr.mqtt.acl.engine.model.matcher.dynamic;

import javasabr.mqtt.model.MqttUser;
import org.jspecify.annotations.Nullable;

public abstract class TopicSegmentResolver {
  
  public static final String START_VARIABLE = "{";
  public static final String END_VARIABLE = "}";
  
  @Nullable
  public abstract String resolve(MqttUser user);
}
