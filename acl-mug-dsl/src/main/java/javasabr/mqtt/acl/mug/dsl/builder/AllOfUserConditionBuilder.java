package javasabr.mqtt.acl.mug.dsl.builder;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import javasabr.mqtt.acl.engine.exception.AclConfigurationException;
import javasabr.mqtt.acl.engine.model.condition.AllOfCondition;
import javasabr.mqtt.acl.engine.model.condition.MqttUserCondition;
import javasabr.mqtt.acl.engine.model.matcher.ValueMatcher;
import javasabr.rlib.collections.array.Array;

public class AllOfUserConditionBuilder extends UserConditionBuilder<AllOfUserConditionBuilder> {

  private final Set<UserIdentity> alreadySetIdentities = new HashSet<>();

  public AllOfUserConditionBuilder anyOf(Consumer<AnyOfUserConditionBuilder> config) {
    conditions.add(new AnyOfUserConditionBuilder()
        .apply(config)
        .build());
    return this;
  }

  @Override
  public AllOfUserConditionBuilder userName(ValueMatcher<String> matcher) {
    requireSingleMatcher(UserIdentity.USER_NAME);
    return super.userName(matcher);
  }

  @Override
  public AllOfUserConditionBuilder clientId(ValueMatcher<String> matcher) {
    requireSingleMatcher(UserIdentity.CLIENT_ID);
    return super.clientId(matcher);
  }

  @Override
  public AllOfUserConditionBuilder ipAddress(ValueMatcher<String> matcher) {
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
