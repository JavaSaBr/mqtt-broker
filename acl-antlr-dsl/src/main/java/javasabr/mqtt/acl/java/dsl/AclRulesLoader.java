package javasabr.mqtt.acl.java.dsl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Consumer;
import javasabr.mqtt.acl.engine.builder.RuleContainerBuilder;
import javasabr.mqtt.acl.engine.exception.AclConfigurationException;
import javasabr.mqtt.acl.engine.model.rule.AclRule;
import javasabr.mqtt.acl.java.dsl.antlr.AclSyntaxErrorListener;
import javasabr.mqtt.acl.java.dsl.antlr.GaclVisitorImpl;
import javasabr.mqtt.acl.java.dsl.builder.AclRulesBuilder;
import javasabr.mqtt.model.acl.Operation;
import javasabr.rlib.collections.array.Array;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

public class AclRulesLoader {

  public static Map<Operation, Array<AclRule>> build(Consumer<AclRulesBuilder> config) {
    AclRulesBuilder builder = new AclRulesBuilder();
    config.accept(builder);
    return RuleContainerBuilder.groupRulesByOperation(builder.build());
  }

  public static Map<Operation, Array<AclRule>> load(InputStream aclConfig) {
    GaclLexer lexer;
    try {
      lexer = new GaclLexer(CharStreams.fromStream(aclConfig));
    } catch (IOException e) {
      throw new AclConfigurationException("Failed to read ACL file:[%s]".formatted(aclConfig), e);
    }
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    GaclParser parser = new GaclParser(tokens);
    parser.addErrorListener(new AclSyntaxErrorListener());
    GaclParser.AclConfigContext tree = parser.aclConfig();
    AclRulesBuilder aclRulesBuilder = new AclRulesBuilder();
    GaclVisitorImpl visitor = new GaclVisitorImpl(aclRulesBuilder);
    visitor.visit(tree);
    return RuleContainerBuilder.groupRulesByOperation(aclRulesBuilder.build());
  }
}
