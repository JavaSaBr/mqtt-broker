package javasabr.mqtt.acl.mug.dsl.loader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Consumer;
import javasabr.mqtt.acl.engine.builder.RuleContainerBuilder;
import javasabr.mqtt.acl.engine.exception.AclConfigurationException;
import javasabr.mqtt.acl.engine.model.rule.AclRule;
import javasabr.mqtt.acl.mug.dsl.builder.AclRulesBuilder;
import javasabr.mqtt.acl.mug.dsl.parser.GaclParser;
import javasabr.mqtt.model.acl.Operation;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.collections.array.ArrayBuilder;

public class AclRulesLoader {

  public static Map<Operation, Array<AclRule>> build(Consumer<AclRulesBuilder> config) {
    return RuleContainerBuilder.groupRulesByOperation(new AclRulesBuilder()
        .apply(config)
        .build());
  }

  public static Map<Operation, Array<AclRule>> load(Path aclConfigPath) {
    if (Files.notExists(aclConfigPath)) {
      throw new AclConfigurationException("Config file:[%s] doesn't exist".formatted(aclConfigPath));
    }
    String content;
    try {
      content = Files.readString(aclConfigPath);
    } catch (IOException e) {
      throw new AclConfigurationException("Failed to read ACL file:[%s]".formatted(aclConfigPath), e);
    }
    Array<AclRule> rules = new ArrayBuilder<>(AclRule.class)
        .add(GaclParser.parse(content))
        .build();
    return RuleContainerBuilder.groupRulesByOperation(rules);
  }
}
