package javasabr.mqtt.acl.service.conifg;

import java.net.URI;
import javasabr.mqtt.acl.antlr.dsl.loader.AclRulesLoader;
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
@ConditionalOnProperty(name = "acl.engine.type", havingValue = "antlr-dsl")
@ConditionalOnClass(name = "javasabr.mqtt.acl.antlr.dsl.loader.AclRulesLoader")
public class AntlrDslBasedAclServiceSpringConfig {

  @Bean
  AuthorizationService authorizationService(@Value("${acl.engine.groovy.dsl.config}") URI aclConfigUri) {
    log.info("Initializing Antlr-DSL based AuthorizationService...");
    var authorizationService = new UriLoaderAuthorizationService(AclRulesLoader::load);
    authorizationService.loadFrom(aclConfigUri);
    return authorizationService;
  }
}
