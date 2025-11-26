package javasabr.mqtt.model.acl.value.matcher;

interface TopicMatcher<T> {

  boolean test(T value)
}
