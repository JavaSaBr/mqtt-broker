package javasabr.mqtt.broker.application.config

import javasabr.mqtt.test.support.UnitSpecification

class MqttTlsSpringConfigTest extends UnitSpecification {

  def 'should map all properties correctly to MqttTlsProperties'() {
    given:
        def config = new MqttTlsSpringConfig()
        
    when:
        def props = config.mqttTlsProperties(
            "ks-path", "ks-pass", "JKS",
            "ts-path", "ts-pass", "PKCS12",
            true, ["TLSv1.2", "TLSv1.3"], ["CIPHER1"]
        )

    then:
        props.keystorePath() == "ks-path"
        props.keystorePassword() == "ks-pass"
        props.keystoreType() == "JKS"
        props.truststorePath() == "ts-path"
        props.truststorePassword() == "ts-pass"
        props.truststoreType() == "PKCS12"
        props.requireClientCert()
        props.tlsProtocols() == ["TLSv1.2", "TLSv1.3"]
        props.cipherSuites() == ["CIPHER1"]
  }
}
