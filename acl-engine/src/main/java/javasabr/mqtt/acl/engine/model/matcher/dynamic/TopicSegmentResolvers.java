package javasabr.mqtt.acl.engine.model.matcher.dynamic;

import java.util.stream.Stream;
import javasabr.rlib.collections.dictionary.DictionaryCollectors;
import javasabr.rlib.collections.dictionary.RefToRefDictionary;
import lombok.CustomLog;

@CustomLog
public class TopicSegmentResolvers {
  
  private static final RefToRefDictionary<String, TopicSegmentResolver> RESOLVERS = buildResolvers();

  public static TopicSegmentResolver findBySegment(String segment) {
    TopicSegmentResolver computableResolver = RESOLVERS.get(segment);
    if (computableResolver == null && containsVariable(segment)) {
      log.warning(segment, "Segment:[%s] looks like variable but doesn't much any resolver"::formatted);
    }
    return computableResolver == null ? new NoOpsTopicSegmentResolver(segment) : computableResolver;
  }
  
  private static boolean containsVariable(String segment) {
    int startIndex = segment.indexOf(TopicSegmentResolver.START_VARIABLE_CHAR);
    return startIndex >= 0 && segment.indexOf(TopicSegmentResolver.END_VARIABLE_CHAR, startIndex) > 0;
  }
  
  private static RefToRefDictionary<String, TopicSegmentResolver> buildResolvers() {
    return Stream
        .of(TopicSegmentResolverType.values())
        .collect(DictionaryCollectors.toRefToRefDictionary(
            TopicSegmentResolverType::pattern,
            TopicSegmentResolverType::resolver));
  }
}
