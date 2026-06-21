package javasabr.mqtt.acl.mug.dsl.loader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import javasabr.mqtt.acl.engine.builder.RuleContainerBuilder;
import javasabr.mqtt.acl.engine.exception.AclConfigurationException;
import javasabr.mqtt.acl.engine.model.rule.AclRule;
import javasabr.mqtt.acl.mug.dsl.parser.GaclParser;
import javasabr.mqtt.model.acl.Operation;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.collections.array.ArrayBuilder;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AclRulesLoader {

  private final GaclParser parser;

  public Map<Operation, Array<AclRule>> load(InputStream aclConfigInputStream) {
    String content;
    try {
      content = new InputStreamReader(aclConfigInputStream, StandardCharsets.UTF_8).readAllAsString();
    } catch (Exception e) {
      throw new AclConfigurationException("Failed to read ACL input stream", e);
    }
    Array<AclRule> rules = new ArrayBuilder<>(AclRule.class)
        .add(parser.parse(content))
        .build();
    return RuleContainerBuilder.groupRulesByOperation(rules);
  }
}
