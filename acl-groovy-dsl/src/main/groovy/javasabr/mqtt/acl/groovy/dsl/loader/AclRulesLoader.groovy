package javasabr.mqtt.acl.groovy.dsl.loader

import javasabr.mqtt.acl.engine.builder.RuleContainerBuilder
import javasabr.mqtt.acl.engine.model.rule.AclRule
import javasabr.mqtt.acl.groovy.dsl.builder.AclRulesBuilder
import javasabr.mqtt.model.acl.Operation
import javasabr.rlib.collections.array.Array

class AclRulesLoader {

  static Map<Operation, Array<AclRule>> load(InputStream aclConfigInputStream) {
    AclRulesBuilder aclRulesBuilder = new AclRulesBuilder()

    def binding = new Binding()
    binding.with {
      setVariable("allowPublish", aclRulesBuilder.&allowPublish)
      setVariable("denyPublish", aclRulesBuilder.&denyPublish)
      setVariable("allowSubscribe", aclRulesBuilder.&allowSubscribe)
      setVariable("denySubscribe", aclRulesBuilder.&denySubscribe)
    }

    def groovyShell = new GroovyShell(binding)
    groovyShell.evaluate(new InputStreamReader(aclConfigInputStream))

    return RuleContainerBuilder.groupRulesByOperation(aclRulesBuilder.build())
  }
}
