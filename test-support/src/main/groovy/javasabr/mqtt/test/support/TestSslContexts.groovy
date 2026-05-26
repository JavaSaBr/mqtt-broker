package javasabr.mqtt.test.support

import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.security.KeyStore

/**
 * Programmatic test certificate generation using keytool.
 * Creates temporary keystores and truststores for TLS integration tests.
 */
class TestSslContexts {

  private static final String PASSWORD = "changeme"
  private static final String KEYSTORE_TYPE = "PKCS12"
  private static final String KEY_ALG = "RSA"
  private static final String KEY_SIZE = "2048"
  private static final String VALIDITY = "365"
  private static final String KEYTOOL_PATH = resolveKeytoolPath()

  private final Path tempDir
  private final Path serverKeystore
  private final Path clientKeystore
  private final Path truststore

  private static class Holder {
    private static final TestSslContexts INSTANCE = new TestSslContexts()
  }

  static TestSslContexts getInstance() {
    return Holder.INSTANCE
  }

  private TestSslContexts() {
    this.tempDir = Files.createTempDirectory("mqtt-tls-test-")
    this.serverKeystore = tempDir.resolve("server.p12")
    this.clientKeystore = tempDir.resolve("client.p12")
    this.truststore = tempDir.resolve("trust.p12")

    generateServerKeystore()
    generateClientKeystore()
    generateTruststore()

    Runtime.getRuntime().addShutdownHook(new Thread({
      cleanup()
    }))
  }

  SSLContext buildServerSslContext() {
    return buildSslContextFromKeystore(serverKeystore, truststore)
  }

  SSLContext buildServerSslContextNoClientAuth() {
    return buildSslContextFromKeystore(serverKeystore, null)
  }

  SSLContext buildClientSslContext() {
    return buildSslContextFromKeystore(clientKeystore, truststore)
  }

  SSLContext buildClientSslContextTrustingServer() {
    KeyStore ts = loadKeyStore(truststore)
    TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
    tmf.init(ts)

    SSLContext ctx = SSLContext.getInstance("TLS")
    ctx.init(null, tmf.trustManagers, null)
    return ctx
  }

  SSLContext buildClientSslContextWithMutualTls() {
    return buildSslContextFromKeystore(clientKeystore, truststore)
  }

  TrustManagerFactory buildTrustManagerFactory() {
    KeyStore ts = loadKeyStore(truststore)
    TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
    tmf.init(ts)
    return tmf
  }

  Path getServerKeystorePath() { return serverKeystore }

  Path getClientKeystorePath() { return clientKeystore }

  String getPassword() { return PASSWORD }

  void cleanup() {
    Files.deleteIfExists(serverKeystore)
    Files.deleteIfExists(clientKeystore)
    Files.deleteIfExists(truststore)
    Files.list(tempDir).forEach { Files.deleteIfExists(it) }
    Files.deleteIfExists(tempDir)
  }

  private void generateServerKeystore() {
    runKeytool(
        "-genkeypair",
        "-alias", "server",
        "-keyalg", KEY_ALG,
        "-keysize", KEY_SIZE,
        "-validity", VALIDITY,
        "-keystore", serverKeystore.toString(),
        "-storetype", KEYSTORE_TYPE,
        "-storepass", PASSWORD,
        "-keypass", PASSWORD,
        "-dname", "CN=localhost,O=MQTT Test,C=US",
        "-ext", "SAN=DNS:localhost,IP:127.0.0.1"
    )
  }

  private void generateClientKeystore() {
    runKeytool(
        "-genkeypair",
        "-alias", "client",
        "-keyalg", KEY_ALG,
        "-keysize", KEY_SIZE,
        "-validity", VALIDITY,
        "-keystore", clientKeystore.toString(),
        "-storetype", KEYSTORE_TYPE,
        "-storepass", PASSWORD,
        "-keypass", PASSWORD,
        "-dname", "CN=test-mqtt-client,O=MQTT Test,C=US"
    )
  }

  private void generateTruststore() {
    runKeytool(
        "-importcert",
        "-noprompt",
        "-alias", "server",
        "-keystore", truststore.toString(),
        "-storetype", KEYSTORE_TYPE,
        "-storepass", PASSWORD,
        "-file", exportCert(serverKeystore, "server").toString()
    )
    runKeytool(
        "-importcert",
        "-noprompt",
        "-alias", "client",
        "-keystore", truststore.toString(),
        "-storetype", KEYSTORE_TYPE,
        "-storepass", PASSWORD,
        "-file", exportCert(clientKeystore, "client").toString()
    )
  }

  private Path exportCert(Path keystore, String alias) {
    Path certFile = tempDir.resolve(alias + ".cer")
    runKeytool(
        "-exportcert",
        "-alias", alias,
        "-keystore", keystore.toString(),
        "-storetype", KEYSTORE_TYPE,
        "-storepass", PASSWORD,
        "-file", certFile.toString()
    )
    return certFile
  }

  private static void runKeytool(String... args) {
    ProcessBuilder pb = new ProcessBuilder([KEYTOOL_PATH] + args.toList())
    pb.redirectErrorStream(true)
    Process process = pb.start()
    int exitCode = process.waitFor()
    if (exitCode != 0) {
      String output = process.getInputStream().text
      throw new RuntimeException("keytool failed (exit ${exitCode}): ${output}")
    }
  }

  private static String resolveKeytoolPath() {
    def javaBinPath = Paths.get(System.getProperty("java.home"), "bin")
    def keytoolFile = ['keytool.exe', 'keytool']
        .collect { javaBinPath.resolve(it).toFile() }
        .find { it.exists() }
    if (keytoolFile) {
      return keytoolFile.absolutePath
    } else {
      throw new RuntimeException("keytool not found")
    }
  }

  private SSLContext buildSslContextFromKeystore(Path keystorePath, Path truststorePath) {
    KeyStore ks = loadKeyStore(keystorePath)
    KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
    kmf.init(ks, PASSWORD.toCharArray())

    TrustManagerFactory tmf = null
    if (truststorePath != null) {
      KeyStore ts = loadKeyStore(truststorePath)
      tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
      tmf.init(ts)
    }

    SSLContext ctx = SSLContext.getInstance("TLS")
    ctx.init(kmf.keyManagers, tmf?.trustManagers, null)
    return ctx
  }

  private static KeyStore loadKeyStore(Path path) {
    KeyStore ks = KeyStore.getInstance(KEYSTORE_TYPE)
    path.withInputStream { stream ->
      ks.load(stream, PASSWORD.toCharArray())
    }
    return ks
  }
}
