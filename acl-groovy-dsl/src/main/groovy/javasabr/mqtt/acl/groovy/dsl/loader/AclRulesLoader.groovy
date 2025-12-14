package javasabr.mqtt.acl.groovy.dsl.loader

import javasabr.mqtt.acl.engine.builder.RuleContainerBuilder
import javasabr.mqtt.acl.engine.exception.AclConfigurationException
import javasabr.mqtt.acl.engine.model.rule.Rule
import javasabr.mqtt.acl.groovy.dsl.builder.AclRulesBuilder
import javasabr.mqtt.model.acl.Operation
import javasabr.rlib.collections.array.Array
import org.codehaus.groovy.control.CompilerConfiguration

import java.nio.file.Files
import java.nio.file.Path

class AclRulesLoader {
  
  static Map<Operation, Array<Rule>> load(String aclConfigPath) {
    return load(Path.of(aclConfigPath))
  }
  
  static Map<Operation, Array<Rule>> load(Path aclConfigPath) {
    if (Files.notExists(aclConfigPath)) {
      throw new AclConfigurationException("Config file:[%s] doesn't exist".formatted(aclConfigPath))
    }
    CompilerConfiguration compilerConfig = new CompilerConfiguration()
    AclRulesBuilder aclRulesBuilder = new AclRulesBuilder()
    new GroovyShell(compilerConfig).with {
      setVariable("allowPublish", aclRulesBuilder.&allowPublish)
      setVariable("denyPublish", aclRulesBuilder.&denyPublish)
      setVariable("allowSubscribe", aclRulesBuilder.&allowSubscribe)
      setVariable("denySubscribe", aclRulesBuilder.&denySubscribe)
      evaluate(aclConfigPath.toFile())
    }
    def allDefinedRules = aclRulesBuilder.build()
    return RuleContainerBuilder.groupRulesByOperation(allDefinedRules)
  }
}
