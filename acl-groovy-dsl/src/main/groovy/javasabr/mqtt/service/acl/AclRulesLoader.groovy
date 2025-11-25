package javasabr.mqtt.service.acl

import javasabr.mqtt.model.acl.Rule
import javasabr.mqtt.model.exception.AclConfigurationException
import javasabr.rlib.collections.array.Array
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer

import java.nio.file.Files
import java.nio.file.Path

class AclRulesLoader {

  private static final AclRulesBuilder newBuilder = new AclRulesBuilder()

  private static final ImportCustomizer importCustomizer = new ImportCustomizer()
      .addStaticStars("javasabr.mqtt.model.acl.Permission")
      .addStaticStars("javasabr.mqtt.model.acl.Action")
  private static final CompilerConfiguration config = new CompilerConfiguration()
      .addCompilationCustomizers(importCustomizer)
  private static final GroovyShell shell = new GroovyShell(config)
  private final Path aclConfigPath

  private AclRulesLoader(URI aclConfigUri) {
    this.aclConfigPath = Path.of(aclConfigUri)
    if (Files.notExists(this.aclConfigPath)) {
      throw new AclConfigurationException("Class loader unable to load resource: %s".formatted(aclConfigPath))
    }
  }

  private AclRulesLoader(String aclConfigPath) {
    this(URI.create(aclConfigPath))
  }

  Array<Rule> load() {
    shell.setVariable("allowPublish", newBuilder.&allowPublish)
    shell.setVariable("denyPublish", newBuilder.&denyPublish)
    shell.setVariable("allowSubscribe", newBuilder.&allowSubscribe)
    shell.setVariable("denySubscribe", newBuilder.&denySubscribe)
    shell.evaluate(aclConfigPath.toFile())
    return newBuilder.build()
  }
}

