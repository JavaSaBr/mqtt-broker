package javasabr.mqtt.model.acl.matcher;

import java.util.regex.Pattern;

public record RegexClientMatcher(Pattern pattern) implements ClientMatcher<String> {

  @Override
  public boolean test(String value) {
    return pattern.matcher(value).matches();
  }
}
