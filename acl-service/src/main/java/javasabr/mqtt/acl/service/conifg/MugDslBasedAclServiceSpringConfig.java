package javasabr.mqtt.acl.service.conifg;

import java.net.URI;
import javasabr.mqtt.acl.mug.dsl.loader.AclRulesLoader;
import javasabr.mqtt.acl.service.impl.UriLoaderAuthorizationService;
import javasabr.mqtt.service.AuthorizationService;
import lombok.CustomLog;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@CustomLog
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "acl.engine.type", havingValue = "mug-dsl")
@ConditionalOnClass(name = "javasabr.mqtt.acl.mug.dsl.loader.AclRulesLoader")
public class MugDslBasedAclServiceSpringConfig {

  @Bean
  AuthorizationService authorizationService(@Value("${acl.engine.config.path}") URI aclConfigUri) {
    log.info("Initializing Mug-DSL based AuthorizationService...");
    var authorizationService = new UriLoaderAuthorizationService(AclRulesLoader::load);
    authorizationService.loadFrom(aclConfigUri);
    return authorizationService;
  }
}
