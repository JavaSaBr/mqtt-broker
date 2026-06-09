package javasabr.mqtt.acl.mug.dsl.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;
import javasabr.mqtt.acl.engine.model.rule.AclRule;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.collections.array.ArrayFactory;
import javasabr.rlib.collections.array.MutableArray;

public class AclRulesBuilder {

  private final List<CompletableFuture<AclRule>> ruleBuilderFutures = new ArrayList<>();

  public Array<AclRule> build() {
    MutableArray<AclRule> rules = ArrayFactory.mutableArray(AclRule.class);
    ruleBuilderFutures.forEach(future -> {
      try {
        rules.add(future.join());
      } catch (CompletionException e) {
        if (e.getCause() instanceof RuntimeException) {
          throw (RuntimeException) e.getCause();
        }
        throw e;
      }
    });
    return rules;
  }

  public AclRulesBuilder allowPublish(Consumer<AclRuleBuilder> config) {
    ruleBuilderFutures.add(startBuilderAsync(new AllowPublishAclRuleBuilder(), config));
    return this;
  }

  public AclRulesBuilder denyPublish(Consumer<AclRuleBuilder> config) {
    ruleBuilderFutures.add(startBuilderAsync(new DenyPublishAclRuleBuilder(), config));
    return this;
  }

  public AclRulesBuilder allowSubscribe(Consumer<AclRuleBuilder> config) {
    ruleBuilderFutures.add(startBuilderAsync(new AllowSubscribeAclRuleBuilder(), config));
    return this;
  }

  public AclRulesBuilder denySubscribe(Consumer<AclRuleBuilder> config) {
    ruleBuilderFutures.add(startBuilderAsync(new DenySubscribeAclRuleBuilder(), config));
    return this;
  }

  private static CompletableFuture<AclRule> startBuilderAsync(AclRuleBuilder builder, Consumer<AclRuleBuilder> config) {
    return CompletableFuture.supplyAsync(() -> {
      config.accept(builder);
      return builder.build();
    });
  }
}
