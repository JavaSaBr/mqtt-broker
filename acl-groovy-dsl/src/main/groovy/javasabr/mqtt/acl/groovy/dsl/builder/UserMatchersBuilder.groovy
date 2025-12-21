//file:noinspection unused
package javasabr.mqtt.acl.groovy.dsl.builder

import javasabr.mqtt.acl.engine.exception.AclConfigurationException
import javasabr.mqtt.acl.engine.model.matcher.UserMatchers
import javasabr.mqtt.acl.engine.model.matcher.ValueMatcher
import javasabr.rlib.collections.array.ArrayFactory
import javasabr.rlib.collections.array.MutableArray

class UserMatchersBuilder {

  MutableArray<? extends ValueMatcher<String>> matchers = ArrayFactory.mutableArray(ValueMatcher)

  UserMatchersBuilder startWith(String prefix) {
    if (matchers.contains(ValueMatcher.MATCH_ANY_STRING)) {
      throw new AclConfigurationException("Already included any value matcher")
    }
    matchers.add(UserMatchers.startWith(prefix))
    return this
  }

  UserMatchersBuilder contains(String substring) {
    if (matchers.contains(ValueMatcher.MATCH_ANY_STRING)) {
      throw new AclConfigurationException("Already included any value matcher")
    }
    matchers.add(UserMatchers.contains(substring))
    return this
  }

  UserMatchersBuilder eq(String string) {
    if (matchers.contains(ValueMatcher.MATCH_ANY_STRING)) {
      throw new AclConfigurationException("Already included any value matcher")
    }
    matchers.add(UserMatchers.eq(string))
    return this
  }

  UserMatchersBuilder regex(String string) {
    if (matchers.contains(ValueMatcher.MATCH_ANY_STRING)) {
      throw new AclConfigurationException("Already included any value matcher")
    }
    matchers.add(UserMatchers.regex(string))
    return this
  }

  UserMatchersBuilder anyValue() {
    if (matchers.contains(ValueMatcher.MATCH_ANY_STRING)) {
      throw new AclConfigurationException("Already included any value matcher")
    }
    matchers.add(ValueMatcher.MATCH_ANY_STRING)
    return this
  }

  UserMatchersBuilder configure(Closure<?> config) {
    config.delegate = this
    config.resolveStrategy = Closure.DELEGATE_ONLY
    config()
    return this
  }

  Collection<ValueMatcher<String>> build() {
    return matchers.toList()
  }
}
