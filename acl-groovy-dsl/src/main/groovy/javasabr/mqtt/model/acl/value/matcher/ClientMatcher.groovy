package javasabr.mqtt.model.acl.value.matcher;

public interface ClientMatcher<T> {

  boolean test(T value);
}
