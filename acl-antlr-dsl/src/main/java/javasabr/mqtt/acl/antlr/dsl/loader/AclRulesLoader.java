package javasabr.mqtt.acl.antlr.dsl.loader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Consumer;
import javasabr.mqtt.acl.engine.builder.RuleContainerBuilder;
import javasabr.mqtt.acl.engine.exception.AclConfigurationException;
import javasabr.mqtt.acl.engine.model.rule.AclRule;
import javasabr.mqtt.acl.java.dsl.GaclLexer;
import javasabr.mqtt.acl.java.dsl.GaclParser;
import javasabr.mqtt.acl.antlr.dsl.builder.AclRulesBuilder;
import javasabr.mqtt.model.acl.Operation;
import javasabr.rlib.collections.array.Array;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

public class AclRulesLoader {

  public static Map<Operation, Array<AclRule>> build(Consumer<AclRulesBuilder> config) {
    AclRulesBuilder builder = new AclRulesBuilder();
    config.accept(builder);
    return RuleContainerBuilder.groupRulesByOperation(builder.build());
  }

  public static Map<Operation, Array<AclRule>> load(Path aclConfigPath) {
    if (Files.notExists(aclConfigPath)) {
      throw new AclConfigurationException("Config file:[%s] doesn't exist".formatted(aclConfigPath));
    }
    GaclLexer lexer;
    try {
      lexer = new GaclLexer(CharStreams.fromPath(aclConfigPath));
    } catch (IOException e) {
      throw new AclConfigurationException("Failed to read ACL file:[%s]".formatted(aclConfigPath), e);
    }
    GaclParser parser = new GaclParser(new CommonTokenStream(lexer));
    parser.addErrorListener(new AclSyntaxErrorListener());
    AclRulesBuilder aclRulesBuilder = new AclRulesBuilder();
    new GaclVisitorImpl(aclRulesBuilder).visit(parser.aclConfig());
    return RuleContainerBuilder.groupRulesByOperation(aclRulesBuilder.build());
  }

  private static class AclSyntaxErrorListener extends BaseErrorListener {

    @Override
    public void syntaxError(
        Recognizer<?, ?> recognizer,
        Object offendingSymbol,
        int line,
        int position,
        String msg,
        RecognitionException e) {
      throw new AclConfigurationException("Syntax error at line %d, column %d: %s".formatted(line, position, msg), e);
    }
  }
}
