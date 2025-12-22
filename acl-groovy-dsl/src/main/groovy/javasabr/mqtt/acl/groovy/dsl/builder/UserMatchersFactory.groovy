package javasabr.mqtt.acl.groovy.dsl.builder

import javasabr.mqtt.acl.engine.model.matcher.EqualsMatcher
import javasabr.mqtt.acl.engine.model.matcher.RegexMatcher
import javasabr.mqtt.acl.engine.model.matcher.StartsWithMatcher
import javasabr.mqtt.acl.engine.model.matcher.ValueMatcher

import java.util.regex.Pattern

abstract class UserMatchersFactory {

  ValueMatcher<String> startsWith(String string) {
    return new StartsWithMatcher(string);
  }

  ValueMatcher<String> eq(String string) {
    return new EqualsMatcher(string);
  }

  ValueMatcher<String> regex(String string) {
    return new RegexMatcher(Pattern.compile(string));
  }

  ValueMatcher<?> anyValue() {
    return ValueMatcher.MATCH_ANY;
  }
}
