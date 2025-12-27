package javasabr.mqtt.acl.engine.model.matcher.dynamic;

import javasabr.mqtt.model.MqttUser;
import org.jspecify.annotations.Nullable;

public abstract sealed class TopicSegmentResolver permits ClientIdTopicSegmentResolver, 
    NoOpsTopicSegmentResolver, UserNameTopicSegmentResolver {
  
  public static final String START_VARIABLE = "{";
  public static final char START_VARIABLE_CHAR = '{';
  
  public static final String END_VARIABLE = "}";
  public static final char END_VARIABLE_CHAR = '}';
  
  @Nullable
  public abstract String resolve(MqttUser user);
}
