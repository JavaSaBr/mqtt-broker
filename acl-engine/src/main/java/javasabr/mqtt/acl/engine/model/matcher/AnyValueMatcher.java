package javasabr.mqtt.acl.engine.model.matcher;

public record AnyValueMatcher() implements ValueMatcher<String> {

  @Override
  public boolean test(String value) {
    return true;
  }
}
