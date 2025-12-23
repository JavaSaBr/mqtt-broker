package javasabr.mqtt.acl.engine.model.matcher.dynamic;

import static javasabr.rlib.collections.dictionary.RefToRefDictionary.entry;

import javasabr.rlib.collections.dictionary.RefToRefDictionary;
import lombok.CustomLog;

@CustomLog
public class TopicSegmentResolvers {
  
  private static final RefToRefDictionary<String, TopicSegmentResolver> RESOLVERS = 
      RefToRefDictionary.ofEntries(
          entry(UserNameTopicSegmentResolver.VARIABLE, new UserNameTopicSegmentResolver()), 
          entry(ClientIdTopicSegmentResolver.VARIABLE, new ClientIdTopicSegmentResolver()));

  public static TopicSegmentResolver findBySegment(String segment) {
    TopicSegmentResolver computableResolver = RESOLVERS.get(segment);
    if (computableResolver == null && isLookingLikeVariable(segment)) {
      log.warning(segment, "Segment:[%s] looks like variable but doesn't much any resolver"::formatted);
    }
    return computableResolver == null ? new NoOpsTopicSegmentResolver(segment) : computableResolver;
  }
  
  private static boolean isLookingLikeVariable(String segment) {
    return segment.contains(TopicSegmentResolver.START_VARIABLE) 
        || segment.contains(TopicSegmentResolver.END_VARIABLE);
  }
}
