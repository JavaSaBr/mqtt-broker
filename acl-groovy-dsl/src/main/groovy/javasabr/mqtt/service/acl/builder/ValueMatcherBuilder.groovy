//file:noinspection unused
package javasabr.mqtt.service.acl.builder

import javasabr.mqtt.model.acl.matcher.EqualsMatcher
import javasabr.mqtt.model.acl.matcher.RegexMatcher
import javasabr.mqtt.model.acl.matcher.TopicFilterMatcher
import javasabr.mqtt.model.acl.matcher.ValueMatcher
import javasabr.mqtt.model.topic.TopicFilter

import java.util.regex.Pattern

interface ValueMatcherBuilder {

  default ValueMatcher<String> eq(String string) {
    new EqualsMatcher(string)
  }

  default ValueMatcher<String> regex(String string) {
    new RegexMatcher(Pattern.compile(string))
  }

  default ValueMatcher<String> match(String string) {
    new TopicFilterMatcher(TopicFilter.valueOf(string));
  }
}
