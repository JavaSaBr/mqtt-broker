package javasabr.mqtt.network;

import java.util.List;
import javasabr.mqtt.base.util.PropertyAssert;
import lombok.Builder;
import org.jspecify.annotations.Nullable;

@Builder
public record TlsProperties(
    String keystorePath,
    String keystorePassword,
    String keystoreType,
    boolean requireClientCert,
    @Nullable String truststorePath,
    @Nullable String truststorePassword,
    @Nullable String truststoreType,
    List<String> tlsProtocols,
    @Nullable List<String> cipherSuites) {
  public TlsProperties {
    PropertyAssert.notBlank(keystorePath, "keystorePath");
    PropertyAssert.notBlank(keystorePassword, "keystorePassword");
    PropertyAssert.notBlank(keystoreType, "keystoreType");
    if (requireClientCert) {
      PropertyAssert.notBlank(truststorePath, "truststorePath");
      PropertyAssert.notBlank(truststorePassword, "truststorePassword");
      PropertyAssert.notBlank(truststoreType, "truststoreType");
    }
    PropertyAssert.notNull(tlsProtocols, "tlsProtocols is null");
    PropertyAssert.positive(tlsProtocols.size(), "tlsProtocols is empty");
    tlsProtocols.forEach(tlsProtocol -> PropertyAssert.notBlank(tlsProtocol, "tlsProtocol"));
    if (cipherSuites != null) {
      cipherSuites.forEach(cipherSuite -> PropertyAssert.notBlank(cipherSuite, "cipherSuite"));
    }
  }
}
