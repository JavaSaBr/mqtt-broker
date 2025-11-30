//file:noinspection unused
package javasabr.mqtt.service.acl.builder;

import java.util.regex.Pattern;
import javasabr.mqtt.model.acl.matcher.EqualsMatcher;
import javasabr.mqtt.model.acl.matcher.RegexMatcher;
import javasabr.mqtt.model.acl.matcher.ValueMatcher;

public interface ClientMatcherBuilder {

  default ValueMatcher<String> eq(String string) {
    return new EqualsMatcher(string);
  }

  default ValueMatcher<String> regex(String string) {
    return new RegexMatcher(Pattern.compile(string));
  }

  default ValueMatcher<?> anyone() {
    return ValueMatcher.ANY;
  }
}
