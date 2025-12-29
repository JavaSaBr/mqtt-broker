package javasabr.mqtt.acl.engine.model.matcher.dynamic;

import java.util.function.Supplier;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Getter
@Accessors
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum TopicSegmentResolverType {
  CLIENT_ID("{clientId}", ClientIdTopicSegmentResolver::new),
  USER_ID("{userName}", UserNameTopicSegmentResolver::new);
  
  String pattern;
  Supplier<? extends TopicSegmentResolver> factory;
  
  public TopicSegmentResolver resolver() {
    return factory.get();
  }
}
