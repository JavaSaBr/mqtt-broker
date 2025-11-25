package javasabr.mqtt.model.acl;

import java.util.Objects;
import java.util.regex.Pattern;

public record RegexComparator(Pattern rulePattern) implements ClientComparator {

  @Override
  public boolean compare(String string) {
    return rulePattern
        .matcher(string)
        .matches();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof RegexComparator(Pattern requestedPattern))) {
      return false;
    }
    return Objects.equals(rulePattern.pattern(), requestedPattern.pattern());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(rulePattern.pattern());
  }
}
