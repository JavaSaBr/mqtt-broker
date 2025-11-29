package javasabr.mqtt.model.acl.condition;

import javasabr.mqtt.model.acl.matcher.AnyValueMatcher;
import javasabr.mqtt.model.acl.matcher.ValueMatcher;
import javasabr.rlib.collections.array.Array;

public record TopicCondition(Array<ValueMatcher<String>> topics) implements Condition<String> {

  public static final TopicCondition MATCH_ANY = new TopicCondition(new AnyValueMatcher());

  public TopicCondition(ValueMatcher<String> topics) {
    this(Array.of(topics));
  }

  @Override
  public boolean test(String value) {
    for (ValueMatcher<String> topic : topics()) {
      if (topic.test(value)) {
        return true;
      }
    }
    return false;
  }
}
