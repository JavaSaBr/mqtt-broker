package javasabr.mqtt.network;

import java.util.List;
import javasabr.mqtt.base.util.PropertyAssert;
import lombok.Builder;
import org.jspecify.annotations.Nullable;

@Builder
public record MqttTlsProperties(
    String keystorePath,
    String keystorePassword,
    String keystoreType,
    boolean requireClientCert,
    @Nullable String truststorePath,
    @Nullable String truststorePassword,
    @Nullable String truststoreType,
    List<String> tlsProtocols,
    @Nullable List<String> cipherSuites) {
  public MqttTlsProperties {
    PropertyAssert.notNull(keystorePath, "keystorePath is null");
    PropertyAssert.notNull(keystorePassword, "keystorePassword is null");
    PropertyAssert.notNull(keystoreType, "keystoreType is null");
    if (requireClientCert) {
      PropertyAssert.notNull(truststorePath, "truststorePath is null");
      PropertyAssert.notNull(truststorePassword, "truststorePassword is null");
      PropertyAssert.notNull(truststoreType, "truststoreType is null");
    }
    PropertyAssert.notNull(tlsProtocols, "tlsProtocols is null");
    tlsProtocols.forEach(tlsProtocol -> PropertyAssert.notEmpty(tlsProtocol, "tlsProtocol is empty"));
    if (cipherSuites != null) {
      cipherSuites.forEach(cipherSuite -> PropertyAssert.notEmpty(cipherSuite, "cipherSuite is empty"));
    }
  }
}
