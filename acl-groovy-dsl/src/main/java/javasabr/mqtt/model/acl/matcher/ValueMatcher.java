package javasabr.mqtt.model.acl.matcher;

public interface ValueMatcher<T> {

  ValueMatcher<?> ANY = new AnyValueMatcher();

  boolean test(T value);
}
