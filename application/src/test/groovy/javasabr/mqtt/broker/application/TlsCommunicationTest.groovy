package javasabr.mqtt.broker.application

import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAckReturnCode
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAckReasonCode
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PayloadFormatIndicator

class TlsCommunicationTest extends TlsIntegrationSpecification {

  def "MQTT 3.1.1 client should connect over TLS"() {
    given:
        def client = buildTlsMqtt311Client()
    when:
        def result = client.connect().join()
    then:
        result.returnCode == Mqtt3ConnAckReturnCode.SUCCESS
    cleanup:
        client.disconnect().join()
  }

  def "MQTT 5 client should connect over TLS"() {
    given:
        def client = buildTlsMqtt5Client()
    when:
        def result = client.connect().join()
    then:
        result.reasonCode == Mqtt5ConnAckReasonCode.SUCCESS
    cleanup:
        client.disconnect().join()
  }

  def "MQTT 3.1.1 client should connect and publish over TLS"() {
    given:
        def publisher = buildTlsMqtt311Client()
        def subscriber = buildTlsMqtt311Client()
        def receivedPayload = null
    when:
        publisher.connect().join()
        subscriber.connect().join()
        subscriber.subscribeWith()
            .topicFilter("tls/test")
            .callback {
              it.payload.ifPresent {
                byte[] bytes = new byte[it.remaining()]
                it.get(bytes)
                receivedPayload = new String(bytes)
              }
            }
            .send()
            .join()
        publisher.publishWith()
            .topic("tls/test")
            .payload("hello-tls".bytes)
            .send()
            .join()
        Thread.sleep(1000)
    then:
        receivedPayload == "hello-tls"
    cleanup:
        publisher.disconnect().join()
        subscriber.disconnect().join()
  }

  def "MQTT 5 client should connect and publish over TLS"() {
    given:
        def publisher = buildTlsMqtt5Client()
        def subscriber = buildTlsMqtt5Client()
        def receivedPayload = null
    when:
        publisher.connect().join()
        subscriber.connect().join()
        subscriber.subscribeWith()
            .topicFilter("tls/test")
            .callback {
              it.payload.ifPresent {
                byte[] bytes = new byte[it.remaining()]
                it.get(bytes)
                receivedPayload = new String(bytes)
              }
            }
            .send()
            .join()
        publisher.publishWith()
            .topic("tls/test")
            .payloadFormatIndicator(Mqtt5PayloadFormatIndicator.UTF_8)
            .payload("hello-tls".bytes)
            .send()
            .join()
        Thread.sleep(1000)
    then:
        receivedPayload == "hello-tls"
    cleanup:
        publisher.disconnect().join()
        subscriber.disconnect().join()
  }

  def "should reject plain TCP connection to TLS port"() {
    when:
        def socket = new Socket(tlsNetworkAddress.hostName, tlsNetworkAddress.port)
        socket.soTimeout = 3000
        def input = socket.getInputStream()
        input.read()
    then:
        thrown(Exception)
    cleanup:
        socket?.close()
  }
}
