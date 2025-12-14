package javasabr.mqtt.acl.engine.model.matcher;

import java.util.regex.Pattern;

public record RegexMatcher(Pattern pattern) implements ValueMatcher<String> {

  @Override
  public boolean test(String value) {
    return pattern.matcher(value).matches();
  }
}
