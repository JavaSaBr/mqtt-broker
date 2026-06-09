package javasabr.mqtt.acl.mug.dsl.builder;

import java.util.Collection;
import javasabr.mqtt.acl.engine.exception.AclConfigurationException;
import javasabr.mqtt.acl.engine.model.matcher.UserMatchers;
import javasabr.mqtt.acl.engine.model.matcher.ValueMatcher;
import javasabr.rlib.collections.array.ArrayFactory;
import javasabr.rlib.collections.array.MutableArray;

/**
 * The builder of user matchers.
 */
public class UserMatchersBuilder {

  private final MutableArray<ValueMatcher<String>> matchers = ArrayFactory.mutableArray(ValueMatcher.class);

  public UserMatchersBuilder startsWith(String prefix) {
    checkAnyValue();
    matchers.add(UserMatchers.startsWith(prefix));
    return this;
  }

  public UserMatchersBuilder contains(String substring) {
    checkAnyValue();
    matchers.add(UserMatchers.contains(substring));
    return this;
  }

  public UserMatchersBuilder eq(String string) {
    checkAnyValue();
    matchers.add(UserMatchers.eq(string));
    return this;
  }

  public UserMatchersBuilder regex(String string) {
    checkAnyValue();
    matchers.add(UserMatchers.regex(string));
    return this;
  }

  public UserMatchersBuilder anyValue() {
    checkAnyValue();
    matchers.add(ValueMatcher.MATCH_ANY_STRING);
    return this;
  }

  private void checkAnyValue() {
    if (matchers.contains(ValueMatcher.MATCH_ANY_STRING)) {
      throw new AclConfigurationException("Already included any value matcher");
    }
  }

  public Collection<ValueMatcher<String>> build() {
    return matchers.toList();
  }
}
