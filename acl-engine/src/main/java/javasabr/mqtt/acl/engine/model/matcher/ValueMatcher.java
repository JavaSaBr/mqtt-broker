package javasabr.mqtt.acl.engine.model.matcher;

public interface ValueMatcher<T> {

  ValueMatcher<?> MATCH_ANY = new AnyValueMatcher();

  boolean test(T value);
}
