package javasabr.mqtt.auth.service.config;

import java.net.URI;
import javasabr.mqtt.auth.api.file.FileProperties;
import javasabr.mqtt.auth.credentials.source.FileCredentialsSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(name = "javasabr.mqtt.auth.credentials.source.FileCredentialsSource")
@ConditionalOnProperty(name = "authentication.credentials-source.file.enabled", havingValue = "true")
public class FileCredentialsSourceSpringConfig {

  @Bean
  FileProperties fileCredentialsSourceProperties(
      @Value("${authentication.credentials-source.file.path}") URI path) {
    return new FileProperties(path);
  }

  @Bean(initMethod = "init")
  FileCredentialsSource fileCredentialsSource(FileProperties fileCredentialsSourceProperties) {
    return new FileCredentialsSource(fileCredentialsSourceProperties.path());
  }
}
