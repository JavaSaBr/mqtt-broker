package javasabr.mqtt.acl.mug.dsl.builder;

import java.util.function.Consumer;
import javasabr.mqtt.acl.engine.model.rule.AclRule;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.collections.array.ArrayBuilder;

/**
 * Builds list of {@link AclRule} from ACL configuration.
 */
public class AclRulesBuilder {

  private final ArrayBuilder<AclRule> rules = new ArrayBuilder<>(AclRule.class);

  public Array<AclRule> build() {
    return rules.build();
  }

  public AclRulesBuilder allowPublish(Consumer<AclRuleBuilder> config) {
    rules.add(new AllowPublishAclRuleBuilder().apply(config).build());
    return this;
  }

  public AclRulesBuilder denyPublish(Consumer<AclRuleBuilder> config) {
    rules.add(new DenyPublishAclRuleBuilder().apply(config).build());
    return this;
  }

  public AclRulesBuilder allowSubscribe(Consumer<AclRuleBuilder> config) {
    rules.add(new AllowSubscribeAclRuleBuilder().apply(config).build());
    return this;
  }

  public AclRulesBuilder denySubscribe(Consumer<AclRuleBuilder> config) {
    rules.add(new DenySubscribeAclRuleBuilder().apply(config).build());
    return this;
  }
}
