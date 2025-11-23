package javasabr.mqtt.service.acl

import javasabr.mqtt.model.acl.Rule
import javasabr.mqtt.model.exception.AclConfigurationException
import javasabr.rlib.collections.array.Array
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer

class AclRulesLoader {

  private static final AclRulesBuilder builder = new AclRulesBuilder()

  private static final ImportCustomizer importCustomizer = new ImportCustomizer()
      .addStaticStars("javasabr.mqtt.model.acl.Permission")
      .addStaticStars("javasabr.mqtt.model.acl.Operator")
      .addStaticStars("javasabr.mqtt.model.acl.Action")
  private static final CompilerConfiguration config = new CompilerConfiguration()
      .addCompilationCustomizers(importCustomizer)
  private static final GroovyShell shell = new GroovyShell(config)

  static {
    shell.setVariable("rule", builder.&rule)
  }

  private AclRulesLoader() {}

  private static File getAclRulesFile() {
    URL resource = AclRulesLoader.class
        .getClassLoader()
        .getResource("acl.groovy")
    if (resource == null) {
      throw new AclConfigurationException("Class loader unable to load resource: acl.groovy")
    }
    File file
    try {
      file = new File(resource.toURI())
    } catch (URISyntaxException e) {
      throw new AclConfigurationException(e)
    }
    if (!file.exists()) {
      throw new AclConfigurationException("File not exists: acl.groovy")
    }
    return file
  }

  static Array<Rule> load() {
    File file = getAclRulesFile()
    shell.evaluate(file)
    return builder.build()
  }
}

