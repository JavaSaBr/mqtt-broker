package javasabr.mqtt.broker.application

import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
import com.hivemq.client.mqtt.mqtt3.message.Mqtt3MessageType
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish
import com.hivemq.client.mqtt.mqtt3.message.subscribe.suback.Mqtt3SubAckReturnCode
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.hivemq.client.mqtt.mqtt5.message.Mqtt5MessageType
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PayloadFormatIndicator
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import com.hivemq.client.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAckReasonCode

import java.util.concurrent.CompletableFuture

class ConnectSubscribePublishTest extends IntegrationSpecification {

  def "should deliver publish message QoS 0 using mqtt 3.1.1"() {
    given:
        def deviceId = generateClientId("device")
        def serviceId = generateClientId("service")
        def serviceName = "ConnectSubscribePublishTest1"
        def received = new CompletableFuture<Mqtt3Publish>()
        def subscriber = buildExternalMqtt311Client(serviceId)
        def publisher = buildExternalMqtt311Client(deviceId)
    when:
        subscriber.connect().join()
        publisher.connect().join()
        def subscribeResult = subscribe(subscriber, "service/$serviceName/device/+", MqttQos.AT_MOST_ONCE, received)
        def publishResult = publish(publisher, "service/$serviceName/device/$deviceId", MqttQos.AT_MOST_ONCE)
    then:
        noExceptionThrown()
        subscribeResult != null
        subscribeResult.returnCodes.contains(Mqtt3SubAckReturnCode.SUCCESS_MAXIMUM_QOS_0)
        subscribeResult.type == Mqtt3MessageType.SUBACK
        publishResult != null
        publishResult.qos == MqttQos.AT_MOST_ONCE
        publishResult.type == Mqtt3MessageType.PUBLISH
        received.join() != null
        received.join().qos == MqttQos.AT_MOST_ONCE
        received.join().type == Mqtt3MessageType.PUBLISH
    cleanup:
        subscriber.disconnect().join()
        publisher.disconnect().join()
  }

  def "should deliver publish message QoS 0 using mqtt 5"() {
    given:
        def deviceId = generateClientId("device")
        def serviceId = generateClientId("service")
        def serviceName = "ConnectSubscribePublishTest2"
        def received = new CompletableFuture<Mqtt5Publish>()
        def subscriber = buildExternalMqtt5Client(serviceId)
        def publisher = buildExternalMqtt5Client(deviceId)
    when:
        subscriber.connect().join()
        publisher.connect().join()
        def subscribeResult = subscribe(subscriber, "service/$serviceName/device/+", MqttQos.AT_MOST_ONCE, received)
        def publishResult = publish(publisher, "service/$serviceName/device/$deviceId", MqttQos.AT_MOST_ONCE)
    then:
        noExceptionThrown()
        subscribeResult != null
        subscribeResult.reasonCodes.contains(Mqtt5SubAckReasonCode.GRANTED_QOS_0)
        subscribeResult.type == Mqtt5MessageType.SUBACK
        publishResult != null
        publishResult.publish.qos == MqttQos.AT_MOST_ONCE
        publishResult.publish.type == Mqtt5MessageType.PUBLISH
        received.join() != null
        received.join().qos == MqttQos.AT_MOST_ONCE
        received.join().type == Mqtt5MessageType.PUBLISH
    cleanup:
        subscriber.disconnect().join()
        publisher.disconnect().join()
  }

  def "should deliver publish message QoS 1 using mqtt 3.1.1"() {
    given:
        def deviceId = generateClientId("device")
        def serviceId = generateClientId("service")
        def serviceName = "ConnectSubscribePublishTest3"
        def received = new CompletableFuture<Mqtt3Publish>()
        def subscriber = buildExternalMqtt311Client(serviceId)
        def publisher = buildExternalMqtt311Client(deviceId)
    when:
        subscriber.connect().join()
        publisher.connect().join()
        def subscribeResult = subscribe(subscriber, "service/$serviceName/device/+", MqttQos.AT_LEAST_ONCE, received)
        def publishResult = publish(publisher, "service/$serviceName/device/$deviceId", MqttQos.AT_LEAST_ONCE)
    then:
        noExceptionThrown()
        subscribeResult != null
        subscribeResult.returnCodes.contains(Mqtt3SubAckReturnCode.SUCCESS_MAXIMUM_QOS_1)
        subscribeResult.type == Mqtt3MessageType.SUBACK
        publishResult != null
        publishResult.qos == MqttQos.AT_LEAST_ONCE
        publishResult.type == Mqtt3MessageType.PUBLISH
        received.join() != null
        received.join().qos == MqttQos.AT_LEAST_ONCE
        received.join().type == Mqtt3MessageType.PUBLISH
    cleanup:
        subscriber.disconnect().join()
        publisher.disconnect().join()
  }

  def "should deliver publish message QoS 1 using mqtt 5"() {
    given:
        def deviceId = generateClientId("device")
        def serviceId = generateClientId("service")
        def serviceName = "ConnectSubscribePublishTest4"
        def received = new CompletableFuture<Mqtt5Publish>()
        def subscriber = buildExternalMqtt5Client(serviceId)
        def publisher = buildExternalMqtt5Client(deviceId)
    when:
        subscriber.connect().join()
        publisher.connect().join()
        def subscribeResult = subscribe(subscriber, "service/$serviceName/device/+", MqttQos.AT_LEAST_ONCE, received)
        def publishResult = publish(publisher, "service/$serviceName/device/$deviceId", MqttQos.AT_LEAST_ONCE)
    then:
        noExceptionThrown()
        subscribeResult != null
        subscribeResult.reasonCodes.contains(Mqtt5SubAckReasonCode.GRANTED_QOS_1)
        subscribeResult.type == Mqtt5MessageType.SUBACK
        publishResult != null
        publishResult.publish.qos == MqttQos.AT_LEAST_ONCE
        publishResult.publish.type == Mqtt5MessageType.PUBLISH
        received.join() != null
        received.join().qos == MqttQos.AT_LEAST_ONCE
        received.join().type == Mqtt5MessageType.PUBLISH
    cleanup:
        subscriber.disconnect().join()
        publisher.disconnect().join()
  }

  def "should deliver publish message QoS 2 using mqtt 3.1.1"() {
    given:
        def deviceId = generateClientId("device")
        def serviceId = generateClientId("service")
        def serviceName = "ConnectSubscribePublishTest5"
        def received = new CompletableFuture<Mqtt3Publish>()
        def subscriber = buildExternalMqtt311Client(serviceId)
        def publisher = buildExternalMqtt311Client(deviceId)
    when:
        subscriber.connect().join()
        publisher.connect().join()
        def subscribeResult = subscribe(subscriber, "service/$serviceName/device/+", MqttQos.EXACTLY_ONCE, received)
        def publishResult = publish(publisher, "service/$serviceName/device/$deviceId", MqttQos.EXACTLY_ONCE)
    then:
        noExceptionThrown()
        subscribeResult != null
        subscribeResult.returnCodes.contains(Mqtt3SubAckReturnCode.SUCCESS_MAXIMUM_QOS_2)
        subscribeResult.type == Mqtt3MessageType.SUBACK
        publishResult != null
        publishResult.qos == MqttQos.EXACTLY_ONCE
        publishResult.type == Mqtt3MessageType.PUBLISH
        received.join() != null
        received.join().qos == MqttQos.EXACTLY_ONCE
        received.join().type == Mqtt3MessageType.PUBLISH
  }

  def "should deliver publish message QoS 2 using mqtt 5"() {
    given:
        def deviceId = generateClientId("device")
        def serviceId = generateClientId("service")
        def serviceName = "ConnectSubscribePublishTest6"
        def received = new CompletableFuture<Mqtt5Publish>()
        def subscriber = buildExternalMqtt5Client(serviceId)
        def publisher = buildExternalMqtt5Client(deviceId)
    when:
        subscriber.connect().join()
        publisher.connect().join()
        def subscribeResult = subscribe(subscriber, "service/$serviceName/device/+", MqttQos.EXACTLY_ONCE, received)
        def publishResult = publish(publisher, "service/$serviceName/device/$deviceId", MqttQos.EXACTLY_ONCE)
        Thread.sleep(100)
    then:
        noExceptionThrown()
        subscribeResult != null
        subscribeResult.reasonCodes.contains(Mqtt5SubAckReasonCode.GRANTED_QOS_2)
        subscribeResult.type == Mqtt5MessageType.SUBACK
        publishResult != null
        publishResult.publish.qos == MqttQos.EXACTLY_ONCE
        publishResult.publish.type == Mqtt5MessageType.PUBLISH
        received.join() != null
        received.join().qos == MqttQos.EXACTLY_ONCE
        received.join().type == Mqtt5MessageType.PUBLISH
    cleanup:
        subscriber.disconnect().join()
        publisher.disconnect().join()
  }

  def publish(Mqtt5AsyncClient publisher, String topicName, MqttQos qos) {
    return publisher.publishWith()
        .topic(topicName)
        .qos(qos)
        .payload(publishPayload)
        .payloadFormatIndicator(Mqtt5PayloadFormatIndicator.UTF_8)
        .send()
        .join()
  }

  def subscribe(
      Mqtt5AsyncClient subscriber,
      String topicFilter,
      MqttQos qos,
      CompletableFuture<Mqtt5Publish> received) {
    return subscriber.subscribeWith()
        .topicFilter(topicFilter)
        .qos(qos)
        .callback({ publish -> received.complete(publish) })
        .send()
        .join()
  }

  def publish(Mqtt3AsyncClient publisher, String topicName, MqttQos qos) {
    return publisher.publishWith()
        .topic(topicName)
        .qos(qos)
        .payload(publishPayload)
        .send()
        .join()
  }

  def subscribe(
      Mqtt3AsyncClient subscriber,
      String topicFilter,
      MqttQos qos,
      CompletableFuture<Mqtt3Publish> received) {
    return subscriber.subscribeWith()
        .topicFilter(topicFilter)
        .qos(qos)
        .callback({ publish -> received.complete(publish) })
        .send()
        .join()
  }
}
