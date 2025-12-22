package javasabr.mqtt.acl.engine.model.matcher;

import java.util.regex.Pattern;

public class UserMatchers {

  public static ValueMatcher<String> startsWith(String prefix) {
    return new StartsWithMatcher(prefix);
  }
  
  public static ValueMatcher<String> contains(String substring) {
    return new ContainsMatcher(substring);
  }

  public static ValueMatcher<String> eq(String string) {
    return new EqualsMatcher<>(string);
  }

  public static ValueMatcher<String> regex(String string) {
    return new RegexMatcher(Pattern.compile(string));
  }

  public static ValueMatcher<String> anyValue() {
    return ValueMatcher.MATCH_ANY_STRING;
  }
}
