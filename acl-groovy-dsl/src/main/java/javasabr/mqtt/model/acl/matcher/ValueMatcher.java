package javasabr.mqtt.model.acl.matcher;

public interface ValueMatcher<T> {

  ValueMatcher<?> MATCH_ANY = new AnyValueMatcher();

  boolean test(T value);
}
