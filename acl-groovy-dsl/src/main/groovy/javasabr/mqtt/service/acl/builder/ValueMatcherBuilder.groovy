package javasabr.mqtt.service.acl.builder;

import java.util.regex.Pattern;
import javasabr.mqtt.model.acl.value.matcher.ClientMatcher;
import javasabr.mqtt.model.acl.value.matcher.EqualsValueMatcher;
import javasabr.mqtt.model.acl.value.matcher.RegexValueMatcher;

interface ValueMatcherBuilder {
    default ClientMatcher<String> eq(String string) {
      new EqualsValueMatcher(string)
    }

    default ClientMatcher<String> regex(String string) {
      new RegexValueMatcher(Pattern.compile(string))
    }
  }
