package javasabr.mqtt.model.acl.value.matcher

record TopicFilterValueMatcher(String expectedValue) implements TopicMatcher<String> {

  @Override
  boolean test(String value) {
    return Objects.equals(expectedValue, value)
  }
}
