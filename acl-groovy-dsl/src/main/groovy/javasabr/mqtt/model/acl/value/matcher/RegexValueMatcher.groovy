package javasabr.mqtt.model.acl.value.matcher;

import java.util.regex.Pattern;

public record RegexValueMatcher(Pattern pattern) implements ClientMatcher<String> {

  @Override
  public boolean test(String value) {
    return pattern.matcher(value).matches();
  }
}
