package javasabr.mqtt.auth.service.config.property;

import javasabr.mqtt.auth.api.AuthenticationMethod;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("authentication.provider.default")
public record DefaultProviderProperties(boolean enabled, AuthenticationMethod method) {}
