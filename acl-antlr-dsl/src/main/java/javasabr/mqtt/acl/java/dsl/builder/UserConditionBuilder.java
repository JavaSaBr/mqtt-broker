package javasabr.mqtt.acl.java.dsl.builder;

import javasabr.mqtt.acl.engine.exception.AclConfigurationException;
import javasabr.mqtt.acl.engine.model.condition.ClientIdCondition;
import javasabr.mqtt.acl.engine.model.condition.IpAddressCondition;
import javasabr.mqtt.acl.engine.model.condition.MqttUserCondition;
import javasabr.mqtt.acl.engine.model.condition.UserNameCondition;
import javasabr.mqtt.acl.engine.model.matcher.UserMatchers;
import javasabr.mqtt.acl.engine.model.matcher.ValueMatcher;
import javasabr.rlib.collections.array.ArrayFactory;
import javasabr.rlib.collections.array.MutableArray;

/**
 * Base class for user condition builders.
 *
 * @param <B> the type of the builder.
 */
@SuppressWarnings("unchecked")
public abstract class UserConditionBuilder<B extends UserConditionBuilder<B>> {

  protected final MutableArray<MqttUserCondition> conditions = ArrayFactory.mutableArray(MqttUserCondition.class);

  public B userName(ValueMatcher<String> matcher) {
    checkAnyUser();
    conditions.add(new UserNameCondition(matcher));
    return (B) this;
  }

  public B clientId(ValueMatcher<String> matcher) {
    checkAnyUser();
    conditions.add(new ClientIdCondition(matcher));
    return (B) this;
  }

  public B ipAddress(ValueMatcher<String> matcher) {
    checkAnyUser();
    conditions.add(new IpAddressCondition(matcher));
    return (B) this;
  }

  public ValueMatcher<String> startsWith(String prefix) {
    return UserMatchers.startsWith(prefix);
  }

  public ValueMatcher<String> contains(String substring) {
    return UserMatchers.contains(substring);
  }

  public ValueMatcher<String> eq(String string) {
    return UserMatchers.eq(string);
  }

  public ValueMatcher<String> regex(String string) {
    return UserMatchers.regex(string);
  }

  public ValueMatcher<String> anyValue() {
    return ValueMatcher.MATCH_ANY_STRING;
  }

  protected void checkAnyUser() {
    if (conditions.contains(MqttUserCondition.MATCH_ANY)) {
      throw new AclConfigurationException("Already included any user condition");
    }
  }

  public abstract MqttUserCondition build();
}
