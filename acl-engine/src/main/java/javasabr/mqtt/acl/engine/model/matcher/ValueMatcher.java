package javasabr.mqtt.acl.engine.model.matcher;

public interface ValueMatcher<T> {

  ValueMatcher<?> MATCH_ANY = AnyValueMatcher.instance();

  boolean test(T value);
}
