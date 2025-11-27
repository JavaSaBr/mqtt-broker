package javasabr.mqtt.model.acl.matcher;

public record AnyTopic() implements TopicMatcher<String> {

  @Override
  public boolean test(String value) {
    return true;
  }
}
