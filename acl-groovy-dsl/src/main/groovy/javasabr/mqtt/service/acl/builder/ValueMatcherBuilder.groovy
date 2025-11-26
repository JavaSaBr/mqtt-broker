//file:noinspection unused
package javasabr.mqtt.service.acl.builder

import javasabr.mqtt.model.acl.matcher.ClientMatcher
import javasabr.mqtt.model.acl.matcher.EqualsClientMatcher
import javasabr.mqtt.model.acl.matcher.RegexClientMatcher

import java.util.regex.Pattern

interface ValueMatcherBuilder {
  default ClientMatcher<String> eq(String string) {
    new EqualsClientMatcher(string)
  }

  default ClientMatcher<String> regex(String string) {
    new RegexClientMatcher(Pattern.compile(string))
  }
}
