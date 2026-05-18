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
    PropertyAssert.notEmpty(keystorePath, "keystorePath is empty");
    PropertyAssert.notEmpty(keystorePassword, "keystorePassword is empty");
    PropertyAssert.notEmpty(keystoreType, "keystoreType is empty");
    if (requireClientCert) {
      PropertyAssert.notEmpty(truststorePath, "truststorePath is empty");
      PropertyAssert.notEmpty(truststorePassword, "truststorePassword is empty");
      PropertyAssert.notEmpty(truststoreType, "truststoreType is empty");
    }
    PropertyAssert.notNull(tlsProtocols, "tlsProtocols is null");
    PropertyAssert.positive(tlsProtocols.size(), "tlsProtocols is empty");
    tlsProtocols.forEach(tlsProtocol -> PropertyAssert.notEmpty(tlsProtocol, "tlsProtocol is empty"));
    if (cipherSuites != null) {
      cipherSuites.forEach(cipherSuite -> PropertyAssert.notEmpty(cipherSuite, "cipherSuite is empty"));
    }
  }
}
