//file:noinspection unused
package javasabr.mqtt.acl.engine.builder;

import java.util.regex.Pattern;
import javasabr.mqtt.acl.engine.model.matcher.EqualsMatcher;
import javasabr.mqtt.acl.engine.model.matcher.RegexMatcher;
import javasabr.mqtt.acl.engine.model.matcher.StartWithMatcher;
import javasabr.mqtt.acl.engine.model.matcher.ValueMatcher;

public interface ClientMatcherBuilder {

  default ValueMatcher<String> startWith(String string) {
    return new StartWithMatcher(string);
  }
  
  default ValueMatcher<String> eq(String string) {
    return new EqualsMatcher(string);
  }

  default ValueMatcher<String> regex(String string) {
    return new RegexMatcher(Pattern.compile(string));
  }

  default ValueMatcher<?> anyone() {
    return ValueMatcher.MATCH_ANY;
  }
}
