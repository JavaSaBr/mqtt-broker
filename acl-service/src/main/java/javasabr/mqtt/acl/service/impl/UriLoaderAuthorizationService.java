package javasabr.mqtt.acl.service.impl;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;
import javasabr.mqtt.acl.engine.AclEngine;
import javasabr.mqtt.acl.engine.exception.AclConfigurationException;
import javasabr.mqtt.acl.engine.model.rule.AclRule;
import javasabr.mqtt.acl.service.AclEngineBasedAuthorizationService;
import javasabr.mqtt.model.acl.Operation;
import javasabr.rlib.collections.array.Array;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@CustomLog
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UriLoaderAuthorizationService extends AclEngineBasedAuthorizationService {

  Function<Path, Map<Operation, Array<AclRule>>> rulesLoader;

  public void loadFrom(URI resource) {
    Path localFile = Path.of(resource);
    if (Files.notExists(localFile)) {
      throw new AclConfigurationException("ACL configuration:[%s] doesn't exist".formatted(resource));
    } else if (Files.isDirectory(localFile)) {
      throw new AclConfigurationException("ACL configuration:[%s] is directory".formatted(resource));
    }
    Map<Operation, Array<AclRule>> loadedAclRulesMap = rulesLoader.apply(localFile);
    switchTo(new AclEngine(loadedAclRulesMap));
    log.info(resource, loadedAclRulesMap, UriLoaderAuthorizationService::buildServiceDescription);
  }

  private static String buildServiceDescription(URI resource, Map<Operation, Array<AclRule>> aclRulesMap) {
    var builder = new StringBuilder();
    builder.append("{\n");
    builder
        .append("  \"SOURCE\":\"")
        .append(resource)
        .append("\",\n");

    int count = 0;
    for (Map.Entry<Operation, Array<AclRule>> entry : aclRulesMap.entrySet()) {
      Operation operation = entry.getKey();
      Array<AclRule> rules = entry.getValue();
      count += rules.size();
      builder
          .append("  \"")
          .append(operation.name())
          .append("\": ")
          .append(rules.size())
          .append(",")
          .append("\n");
    }
    builder
        .delete(builder.length() - 2, builder.length())
        .append("\n}");

    return "Loaded total [%s] ACL rules: %s".formatted(count, builder);
  }
}
