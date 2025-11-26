package javasabr.mqtt.model.acl.value.matcher

import groovy.transform.ImmutableOptions

import java.util.regex.Pattern

@ImmutableOptions(knownImmutableClasses = Pattern)
record RegexValueMatcher(Pattern pattern) implements ClientMatcher<String> {

  @Override
  boolean test(String value) {
    return pattern.matcher(value).matches()
  }
}
