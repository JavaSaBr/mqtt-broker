package javasabr.mqtt.model.acl.value.matcher;

interface ClientMatcher<T> {

  boolean test(T value)
}
