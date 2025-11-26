package javasabr.mqtt.model.acl.value.matcher

record EqualsValueMatcher(String expectedValue) implements ClientMatcher<String> {

  @Override
  boolean test(String value) {
    return Objects.equals(expectedValue, value)
  }
}
