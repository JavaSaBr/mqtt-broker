package javasabr.mqtt.model.acl.matcher;

public record AnyValueMatcher() implements ValueMatcher<String> {

  @Override
  public boolean test(String value) {
    return true;
  }
}
