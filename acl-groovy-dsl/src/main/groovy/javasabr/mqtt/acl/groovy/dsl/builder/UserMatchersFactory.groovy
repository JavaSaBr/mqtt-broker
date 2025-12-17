package javasabr.mqtt.acl.groovy.dsl.builder

import javasabr.mqtt.acl.engine.model.matcher.EqualsMatcher
import javasabr.mqtt.acl.engine.model.matcher.RegexMatcher
import javasabr.mqtt.acl.engine.model.matcher.StartWithMatcher
import javasabr.mqtt.acl.engine.model.matcher.ValueMatcher

import java.util.regex.Pattern

class UserMatchersFactory {

  ValueMatcher<String> startWith(String string) {
    return new StartWithMatcher(string);
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
