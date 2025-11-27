package javasabr.mqtt.service.acl

import javasabr.mqtt.model.acl.rule.Rule
import javasabr.mqtt.model.exception.AclConfigurationException
import javasabr.mqtt.service.acl.builder.AclRulesBuilder
import javasabr.rlib.collections.array.Array
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer

import java.nio.file.Files
import java.nio.file.Path

class AclRulesLoader {

  private static final String[] MODEL_IMPORTS = ["javasabr.mqtt.model.acl.Operation", "javasabr.mqtt.model.acl.Action"]

  private final Path aclConfigPath

  private AclRulesLoader(String aclConfigPath) {
    this.aclConfigPath = Path.of(aclConfigPath)
    if (Files.notExists(this.aclConfigPath)) {
      throw new AclConfigurationException("Class loader unable to load resource: %s".formatted(this.aclConfigPath))
    }
  }

  Array<Rule> load() {
    ImportCustomizer importCustomizer = new ImportCustomizer().addStaticStars(MODEL_IMPORTS)
    CompilerConfiguration compilerConfig = new CompilerConfiguration().addCompilationCustomizers(importCustomizer)
    AclRulesBuilder aclRulesBuilder = new AclRulesBuilder()
    GroovyShell groovyShell = new GroovyShell(compilerConfig)
    groovyShell.setVariable("allowPublish", aclRulesBuilder.&allowPublish)
    groovyShell.setVariable("denyPublish", aclRulesBuilder.&denyPublish)
    groovyShell.setVariable("allowSubscribe", aclRulesBuilder.&allowSubscribe)
    groovyShell.setVariable("denySubscribe", aclRulesBuilder.&denySubscribe)
    groovyShell.evaluate(aclConfigPath.toFile())
    return aclRulesBuilder.build()
  }
}
