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

  @SuppressWarnings('GrFinalVariableAccess')
  private final Path aclConfigPath

  AclRulesLoader(String aclConfigPath) {
    if (aclConfigPath == null) {
      throw new AclConfigurationException("ACL config path is null")
    }
    this.aclConfigPath = Path.of(aclConfigPath)
    if (Files.notExists(this.aclConfigPath)) {
      throw new AclConfigurationException("Class loader unable to load resource: %s".formatted(aclConfigPath))
    }
  }

  Map<Operation, Array<Rule>> load() {
    CompilerConfiguration compilerConfig = new CompilerConfiguration()
    AclRulesBuilder aclRulesBuilder = new AclRulesBuilder()
    new GroovyShell(compilerConfig).with {
      setVariable("allowPublish", aclRulesBuilder.&allowPublish)
      setVariable("denyPublish", aclRulesBuilder.&denyPublish)
      setVariable("allowSubscribe", aclRulesBuilder.&allowSubscribe)
      setVariable("denySubscribe", aclRulesBuilder.&denySubscribe)
      evaluate(aclConfigPath.toFile())
    }
    def rules = aclRulesBuilder.build()
    return RuleContainerBuilder.groupRulesByOperation(rules)
  }
}
