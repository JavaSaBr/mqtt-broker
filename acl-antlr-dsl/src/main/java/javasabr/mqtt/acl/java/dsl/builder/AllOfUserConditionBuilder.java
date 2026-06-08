package javasabr.mqtt.acl.java.dsl.builder;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import javasabr.mqtt.acl.engine.exception.AclConfigurationException;
import javasabr.mqtt.acl.engine.model.condition.AllOfCondition;
import javasabr.mqtt.acl.engine.model.condition.MqttUserCondition;
import javasabr.rlib.collections.array.Array;

/**
 * The builder of allOf user condition.
 */
public class AllOfUserConditionBuilder extends UserConditionBuilder<AllOfUserConditionBuilder> {

  private final Set<UserIdentity> alreadySetIdentities = new HashSet<>();

  public AllOfUserConditionBuilder anyOf(Consumer<AnyOfUserConditionBuilder> config) {
    AnyOfUserConditionBuilder builder = new AnyOfUserConditionBuilder();
    config.accept(builder);
    conditions.add(builder.build());
    return this;
  }

  @Override
  public AllOfUserConditionBuilder userName(javasabr.mqtt.acl.engine.model.matcher.ValueMatcher<String> matcher) {
    requireSingleMatcher(UserIdentity.USER_NAME);
    return super.userName(matcher);
  }

  @Override
  public AllOfUserConditionBuilder clientId(javasabr.mqtt.acl.engine.model.matcher.ValueMatcher<String> matcher) {
    requireSingleMatcher(UserIdentity.CLIENT_ID);
    return super.clientId(matcher);
  }

  @Override
  public AllOfUserConditionBuilder ipAddress(javasabr.mqtt.acl.engine.model.matcher.ValueMatcher<String> matcher) {
    requireSingleMatcher(UserIdentity.IP_ADDRESS);
    return super.ipAddress(matcher);
  }

  private void requireSingleMatcher(UserIdentity userIdentity) {
    if (alreadySetIdentities.contains(userIdentity)) {
      throw new AclConfigurationException("AllOf condition can only have single-matcher members");
    } else {
      alreadySetIdentities.add(userIdentity);
    }
  }

  @Override
  public MqttUserCondition build() {
    return new AllOfCondition(Array.copyOf(conditions));
  }
}
