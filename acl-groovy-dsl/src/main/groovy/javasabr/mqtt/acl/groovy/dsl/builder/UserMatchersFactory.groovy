//file:noinspection unused
package javasabr.mqtt.acl.groovy.dsl.builder

import javasabr.mqtt.acl.engine.model.matcher.AnyValueMatcher
import javasabr.mqtt.acl.engine.model.matcher.UserMatchers
import javasabr.mqtt.acl.engine.model.matcher.ValueMatcher

abstract class UserMatchersFactory {

  ValueMatcher<String> startsWith(String prefix) {
    return UserMatchers.startsWith(prefix)
  }

  ValueMatcher<String> contains(String substring) {
    return UserMatchers.contains(substring)
  }

  ValueMatcher<String> eq(String string) {
    return UserMatchers.eq(string)
  }

  ValueMatcher<String> regex(String string) {
    return UserMatchers.regex(string)
  }

  ValueMatcher<String> anyValue() {
    return ValueMatcher.MATCH_ANY_STRING
  }

  ValueMatcher<String> replaceMatcherIfNeed(ValueMatcher<String> it) {
    return it instanceof AnyValueMatcher ? ValueMatcher.MATCH_ANY_STRING : it
  }
}
  
