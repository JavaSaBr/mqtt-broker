package javasabr.mqtt.model.acl.value.matcher;

public interface TopicMatcher<T> {

  boolean test(T value);
}
