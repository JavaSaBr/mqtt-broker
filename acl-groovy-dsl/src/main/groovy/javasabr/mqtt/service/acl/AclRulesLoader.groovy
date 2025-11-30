package javasabr.mqtt.service.acl

import javasabr.mqtt.model.acl.Operation
import javasabr.mqtt.model.acl.rule.Rule
import javasabr.mqtt.model.exception.AclConfigurationException
import javasabr.mqtt.service.acl.builder.AclRulesBuilder
import javasabr.rlib.collections.array.Array
import javasabr.rlib.collections.array.ArrayFactory
import javasabr.rlib.collections.array.MutableArray
import org.codehaus.groovy.control.CompilerConfiguration

import java.nio.file.Files
import java.nio.file.Path

class AclRulesLoader {

  private Path aclConfigPath = null

  AclRulesLoader(String aclConfigPath) {
    if (aclConfigPath == null) {
      throw new AclConfigurationException("ACL config path is null")
    }
    this.aclConfigPath = Path.of(aclConfigPath)
    if (Files.notExists(this.aclConfigPath)) {
      throw new AclConfigurationException("Class loader unable to load resource: %s".formatted(this.aclConfigPath))
    }
  }

  Map<Operation, Array<Rule>> load() {
    CompilerConfiguration compilerConfig = new CompilerConfiguration()
    try (AclRulesBuilder aclRulesBuilder = new AclRulesBuilder()) {
      new GroovyShell(compilerConfig).with {
        setVariable("allowPublish", aclRulesBuilder.&allowPublish)
        setVariable("denyPublish", aclRulesBuilder.&denyPublish)
        setVariable("allowSubscribe", aclRulesBuilder.&allowSubscribe)
        setVariable("denySubscribe", aclRulesBuilder.&denySubscribe)
        evaluate(aclConfigPath.toFile())
      }
      def rules = aclRulesBuilder.build()
      var intermediate = new EnumMap<Operation, MutableArray<Rule>>(Operation)
      for (Rule rule : rules) {
        intermediate.computeIfAbsent(rule.operation(), AclRulesLoader::newMutableArray).add(rule)
      }
      var finalMap = new EnumMap<Operation, Array<Rule>>(Operation);
      for (var entry : intermediate.entrySet()) {
        finalMap.put(entry.key, Array.copyOf(entry.value))
      }
      Operation.forEach(operation -> {
        finalMap.computeIfAbsent(operation, AclRulesLoader::emptyArray)
      })
      return Collections.unmodifiableMap(finalMap)
    }
  }

  static <K, V> V emptyArray(K ignored) {
    Array.of() as V
  }

  static MutableArray<Rule> newMutableArray(Operation ignored) {
    ArrayFactory.mutableArray(Rule)
  }
}
