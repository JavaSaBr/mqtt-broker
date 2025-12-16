package javasabr.mqtt.acl.engine.model.matcher;

public record StartWithMatcher(String prefix) implements ValueMatcher<String> {

  @Override
  public boolean test(String value) {
    return value.startsWith(prefix);
  }
}
