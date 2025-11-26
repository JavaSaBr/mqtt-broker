package javasabr.mqtt.model.acl.matcher;

public interface ClientMatcher<T> {

  boolean test(T value);
}
