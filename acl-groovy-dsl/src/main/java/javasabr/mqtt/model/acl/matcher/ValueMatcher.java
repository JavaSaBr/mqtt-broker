package javasabr.mqtt.model.acl.matcher;

public interface ValueMatcher<T> {

  ValueMatcher<String> ANY = new AnyValueMatcher();

  boolean test(T value);
}
