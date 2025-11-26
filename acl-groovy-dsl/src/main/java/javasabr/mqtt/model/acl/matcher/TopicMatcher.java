package javasabr.mqtt.model.acl.matcher;

public interface TopicMatcher<T> {

  boolean test(T value);
}
