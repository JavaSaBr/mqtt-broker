package javasabr.mqtt.acl.engine.model.matcher.dynamic;

import javasabr.mqtt.acl.engine.model.matcher.TopicMatcher;
import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.topic.AbstractTopic;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.mqtt.model.topic.TopicValidator;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.experimental.FieldDefaults;

@CustomLog
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public abstract class DynamicTopicMatcher<T extends AbstractTopic> implements TopicMatcher {

  public static TopicMatcher autoBuild(String rawOriginalTopic) {
    if (TopicValidator.validateTopicName(rawOriginalTopic)) {
      return new DynamicTopicNameMatcher(TopicName.valueOf(rawOriginalTopic));
    } else if (TopicValidator.validateTopicFilter(rawOriginalTopic)) {
      return new DynamicTopicFilterMatcher(TopicFilter.valueOf(rawOriginalTopic));
    }
    throw new IllegalArgumentException("Invalid topic:" + rawOriginalTopic);
  }
  
  T originalTopic;
  TopicSegmentResolver[] resolvers;

  protected DynamicTopicMatcher(T originalTopic) {
    String[] rawSegments = originalTopic.segments();
    int segmentsCount = rawSegments.length;
    TopicSegmentResolver[] resolvers = new TopicSegmentResolver[segmentsCount];
    for (int i = 0; i < segmentsCount; i++) {
      resolvers[i] = TopicSegmentResolvers.findBySegment(rawSegments[i]);
    }
    this.originalTopic = originalTopic;
    this.resolvers = resolvers;
  }

  @Override
  public boolean test(MqttUser user, AbstractTopic topic) {
    int segmentsCount = resolvers.length;
    String[] resolvedSegments = new String[segmentsCount];
    for (int i = 0; i < segmentsCount; i++) {
      String resolvedSegment = resolvers[i].resolve(user);
      if (resolvedSegment == null) {
        log.debug(user.clientId(), "[%s] Cannot calculate final topic by unresolved segment"::formatted);
        return false;
      }
      resolvedSegments[i] = resolvedSegment;
    }
    String rawTopic = String.join(AbstractTopic.DELIMITER, resolvedSegments);
    T constructedExpectedTopic = constructTopic(resolvedSegments, rawTopic);
    return constructedExpectedTopic.isMatched(topic);
  }

  protected abstract T constructTopic(String[] segments, String rawTopic);
}
