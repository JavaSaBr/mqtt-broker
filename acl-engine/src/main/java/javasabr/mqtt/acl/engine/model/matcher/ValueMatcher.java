package javasabr.mqtt.acl.engine.model.matcher;

public interface ValueMatcher<T> {

  ValueMatcher<String> MATCH_ANY_STRING = AnyValueMatcher.stringMatcher();

  boolean test(T value);
}
