//file:noinspection unused
package javasabr.mqtt.acl.groovy.dsl.builder

import groovy.transform.TypeChecked
import javasabr.mqtt.acl.engine.model.rule.AclRule
import javasabr.rlib.collections.array.Array
import javasabr.rlib.collections.array.ArrayFactory

import java.util.concurrent.CompletableFuture

/**
 * Builds list of {@link AclRule} from ACL configuration
 */
class AclRulesBuilder {

  private final List<CompletableFuture<AclRule>> ruleBuilderFutures = []

  Array<AclRule> build() {
    return ruleBuilderFutures.collect(ArrayFactory.mutableArray(AclRule.class), { it.join() })
  }
  
  @TypeChecked
  AclRulesBuilder allowPublish(Closure<?> config) {
    ruleBuilderFutures.add(startBuilderAsync(new AllowPublishAclRuleBuilder(), config))
    return this
  }

  @TypeChecked
  AclRulesBuilder denyPublish(Closure<?> config) {
    ruleBuilderFutures.add(startBuilderAsync(new DenyPublishAclRuleBuilder(), config))
    return this
  }

  @TypeChecked
  AclRulesBuilder allowSubscribe(Closure<?> config) {
    ruleBuilderFutures.add(startBuilderAsync(new AllowSubscribeAclRuleBuilder(), config))
    return this
  }

  @TypeChecked
  AclRulesBuilder denySubscribe(Closure<?> config) {
    ruleBuilderFutures.add(startBuilderAsync(new DenySubscribeAclRuleBuilder(), config))
    return this
  }

  private static CompletableFuture<AclRule> startBuilderAsync(AclRuleBuilder builder, Closure<?> config) {
    CompletableFuture.supplyAsync({ putConfigToBuilder(builder, config).build() })
  }

  private static AclRuleBuilder putConfigToBuilder(
      AclRuleBuilder ruleBuilder,
      Closure<?> config) {
    config.delegate = ruleBuilder
    config.resolveStrategy = Closure.DELEGATE_ONLY
    config()
    return ruleBuilder
  }
}
