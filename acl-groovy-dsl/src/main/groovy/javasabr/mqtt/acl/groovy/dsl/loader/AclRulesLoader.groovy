package javasabr.mqtt.acl.groovy.dsl.loader

import javasabr.mqtt.acl.engine.builder.RuleContainerBuilder
import javasabr.mqtt.acl.engine.exception.AclConfigurationException
import javasabr.mqtt.acl.engine.model.rule.AclRule
import javasabr.mqtt.acl.groovy.dsl.builder.AclRulesBuilder
import javasabr.mqtt.model.acl.Operation
import javasabr.rlib.collections.array.Array

import java.nio.file.Files
import java.nio.file.Path

class AclRulesLoader {
  
  static Map<Operation, Array<AclRule>> load(String aclConfigPath) {
    return load(Path.of(aclConfigPath))
  }
  
  static Map<Operation, Array<AclRule>> load(Path aclConfigPath) {
    if (Files.notExists(aclConfigPath)) {
      throw new AclConfigurationException("Config file:[%s] doesn't exist".formatted(aclConfigPath))
    }
    
    AclRulesBuilder aclRulesBuilder = new AclRulesBuilder()

    def binding = new Binding()
    binding.with {
      setVariable("allowPublish", aclRulesBuilder.&allowPublish)
      setVariable("denyPublish", aclRulesBuilder.&denyPublish)
      setVariable("allowSubscribe", aclRulesBuilder.&allowSubscribe)
      setVariable("denySubscribe", aclRulesBuilder.&denySubscribe)
    }

    def groovyShell = new GroovyShell(binding)
    groovyShell.evaluate(aclConfigPath.toFile())
    
    return RuleContainerBuilder.groupRulesByOperation(aclRulesBuilder.build())
  }
}
