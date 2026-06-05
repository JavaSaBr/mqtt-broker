package javasabr.mqtt.broker.application

import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
import com.hivemq.client.mqtt.mqtt3.exceptions.Mqtt3ConnAckException
import com.hivemq.client.mqtt.mqtt3.message.Mqtt3MessageType
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAckReturnCode
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish
import com.hivemq.client.mqtt.mqtt3.message.subscribe.suback.Mqtt3SubAckReturnCode
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5ConnAckException
import com.hivemq.client.mqtt.mqtt5.message.Mqtt5MessageType
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAckReasonCode
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PayloadFormatIndicator
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import com.hivemq.client.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAckReasonCode
import groovy.util.logging.Slf4j
import javasabr.mqtt.model.*
import javasabr.mqtt.model.reason.code.ConnectAckReasonCode
import javasabr.mqtt.model.reason.code.PublishCompletedReasonCode
import javasabr.mqtt.model.reason.code.PublishReceivedReasonCode
import javasabr.mqtt.model.reason.code.SubscribeAckReasonCode
import javasabr.mqtt.model.subscription.Subscription
import javasabr.mqtt.model.topic.TopicFilter
import javasabr.mqtt.network.MqttConnection
import javasabr.mqtt.network.MqttMockClient
import javasabr.mqtt.network.message.in.ConnectAckMqttInMessage
import javasabr.mqtt.network.message.in.PublishMqttInMessage
import javasabr.mqtt.network.message.in.PublishReleaseMqttInMessage
import javasabr.mqtt.network.message.in.SubscribeAckMqttInMessage
import javasabr.mqtt.network.message.out.*
import javasabr.mqtt.network.user.ConfigurableNetworkMqttUser
import javasabr.rlib.collections.array.Array
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

import static javasabr.mqtt.broker.application.MqttClientFactory.generateClientId

@Slf4j
class NativeImageVerificationTest extends Specification {

  private static final String BINARY_PATH = "build/native/nativeCompile/application"
  private static final String NETWORK_READY_MARKER = "Started external MQTT network by address"

  public static final encoding = StandardCharsets.UTF_8
  public static final publishPayload = "publishPayload".getBytes(encoding)
  public static final testClientId = "testClientId"
  public static final keepAlive = 120

  @Shared
  Process brokerProcess

  def setupSpec() {
    brokerProcess = startBroker()
  }

  private static Process startBroker() {
    def binaryPath = new File(BINARY_PATH).absolutePath
    Process process = new ProcessBuilder()
        .command([
            binaryPath,
            //"-Dlog4j.configurationFile=classpath:log4j2.xml",
            //"--debug"
        ])
        .directory(new File("build/native/nativeCompile/"))
        .redirectErrorStream(true)
        .start()

    def networkReady = new CountDownLatch(1)

    Thread.startDaemon("broker-output-drainer") {
      def reader = new BufferedReader(new InputStreamReader(process.getInputStream()))
      String line
      while ((line = reader.readLine()) != null) {
        println(line)
        if (line.contains(NETWORK_READY_MARKER)) {
          networkReady.countDown()
        }
      }
    }

    if (!networkReady.await(10, TimeUnit.SECONDS)) {
      process.destroyForcibly()
      throw new RuntimeException("Broker failed to start within 10 seconds: network not ready")
    }

    return process
  }

  def cleanupSpec() {
    if (brokerProcess != null && brokerProcess.isAlive()) {
      brokerProcess.destroy()
      brokerProcess.waitFor(5, TimeUnit.SECONDS)
      if (brokerProcess.isAlive()) {
        brokerProcess.destroyForcibly()
      }
    }
  }

  def buildExternalMqtt311Client() {
    return buildExternalMqtt311Client(generateClientId("mqtt311"))
  }

  def buildExternalMqtt5Client() {
    return buildExternalMqtt5Client(generateClientId("mqtt5"))
  }

  def buildExternalMqtt311Client(String clientId) {
    return MqttClient.builder()
        .identifier(clientId)
        .serverHost("localhost")
        .serverPort(1883)
        .useMqttVersion3()
        .addDisconnectedListener {
          println "[$clientId|mqtt311] disconnected:[${it.cause?.message}]"
        }
        .buildAsync()
  }

  def buildExternalMqtt5Client(String clientId) {
    return MqttClient.builder()
        .identifier(clientId)
        .serverHost("localhost")
        .serverPort(1883)
        .useMqttVersion5()
        .addDisconnectedListener {
          println "[$clientId|mqtt5] disconnected:[${it.cause?.message}]"
        }
        .buildAsync()
  }

  def connectWith(Mqtt3AsyncClient client, String user, String pass) {
    return client.connectWith()
        .simpleAuth()
        .username(user)
        .password(pass.getBytes(encoding))
        .applySimpleAuth()
        .send()
        .join()
  }

  def connectWith(Mqtt5AsyncClient client, String user, String pass) {
    return client.connectWith()
        .simpleAuth()
        .username(user)
        .password(pass.getBytes(encoding))
        .applySimpleAuth()
        .send()
        .join()
  }

  def buildMqtt5MockClient() {
    return new MqttMockClient(
        "localhost",
        1883,
        mqtt5MockedConnection()
    )
  }

  def buildMqtt311MockClient() {
    return new MqttMockClient(
        "localhost",
        1883,
        mqtt311MockedConnection()
    )
  }

  def mqtt5MockedConnection() {
    def serverConnConfig = new MqttServerConnectionConfig(
        QoS.EXACTLY_ONCE,
        MqttProperties.MAX_MESSAGE_SIZE_DEFAULT,
        MqttProperties.MAX_MESSAGE_SIZE_DEFAULT / 2 as int,
        MqttProperties.MAX_MESSAGE_SIZE_DEFAULT,
        10,
        MqttProperties.SERVER_KEEP_ALIVE_MIN,
        10,
        MqttProperties.TOPIC_ALIAS_MAX,
        true,
        true,
        true,
        true,
        true,
        true
    )
    MqttClientConnectionConfig clientConnConfig = new MqttClientConnectionConfig(
        serverConnConfig,
        serverConnConfig.maxQos(),
        MqttVersion.MQTT_5,
        null,
        serverConnConfig.receiveMaxPublishes(),
        serverConnConfig.maxMessageSize(),
        serverConnConfig.topicAliasMaxValue(),
        MqttProperties.SERVER_KEEP_ALIVE_DEFAULT,
        false,
        false)
    def connectionRef = new AtomicReference<MqttConnection>()
    def connection = Stub(MqttConnection) {
      isSupported(MqttVersion.MQTT_5) >> true
      isSupported(MqttVersion.MQTT_3_1_1) >> true
      serverConnectionConfig() >> serverConnConfig
      clientConnectionConfig() >> clientConnConfig
      user() >> Stub(ConfigurableNetworkMqttUser) {
        connectionConfig() >> clientConnConfig
        connection() >> connectionRef.get()
        clientId() >> testClientId
      }
    }
    connectionRef.set(connection)
    return connection
  }

  def mqtt311MockedConnection() {
    def serverConnConfig = new MqttServerConnectionConfig(
        QoS.EXACTLY_ONCE,
        MqttProperties.MAX_MESSAGE_SIZE_DEFAULT,
        MqttProperties.MAX_MESSAGE_SIZE_DEFAULT / 2 as int,
        MqttProperties.MAX_MESSAGE_SIZE_DEFAULT,
        10,
        MqttProperties.SERVER_KEEP_ALIVE_MIN,
        10,
        MqttProperties.TOPIC_ALIAS_MAX,
        true,
        true,
        true,
        true,
        true,
        true
    )
    MqttClientConnectionConfig clientConnConfig = new MqttClientConnectionConfig(
        serverConnConfig,
        serverConnConfig.maxQos(),
        MqttVersion.MQTT_3_1_1,
        null,
        serverConnConfig.receiveMaxPublishes(),
        serverConnConfig.maxMessageSize(),
        serverConnConfig.topicAliasMaxValue(),
        MqttProperties.SERVER_KEEP_ALIVE_DEFAULT,
        false,
        false)
    def connectionRef = new AtomicReference<MqttConnection>()
    def connection = Stub(MqttConnection) {
      isSupported(MqttVersion.MQTT_5) >> false
      isSupported(MqttVersion.MQTT_3_1_1) >> true
      serverConnectionConfig() >> serverConnConfig
      clientConnectionConfig() >> clientConnConfig
      user() >> Stub(ConfigurableNetworkMqttUser) {
        connectionConfig() >> clientConnConfig
        connection() >> connectionRef.get()
        clientId() >> testClientId
      }
    }
    connectionRef.set(connection)
    return connection
  }

  def "should connect to native image"() {
    given:
        def client = MqttClient.builder()
            .identifier(generateClientId("TLS5"))
            .serverHost("localhost")
            .serverPort(1883)
            .useMqttVersion5()
            .addDisconnectedListener {
              println "[mqtt5] disconnected:[${it.cause?.message}]"
            }
            .buildAsync()
    when:
        def result = client.connect().join()
    then:
        result.reasonCode == Mqtt5ConnAckReasonCode.SUCCESS
    cleanup:
        client.disconnect().join()
  }

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
    cleanup:
        subscriber.disconnect().join()
        publisher.disconnect().join()
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

  def "client should connect to broker without user and pass using MQTT 3.1.1"() {
    given:
        def client = buildExternalMqtt311Client()
    when:
        def result = client.connect().join()
    then:
        result.returnCode == Mqtt3ConnAckReturnCode.SUCCESS
        !result.sessionPresent
    cleanup:
        client.disconnect().join()
  }

  def "client should connect to broker without user and pass using MQTT 5"() {
    given:
        def client = buildExternalMqtt5Client()
    when:
        def result = client.connect().join()
    then:
        result.reasonCode == Mqtt5ConnAckReasonCode.SUCCESS
        !result.sessionExpiryInterval.present
        result.serverKeepAlive.present
        result.serverKeepAlive.getAsInt() == MqttProperties.SERVER_KEEP_ALIVE_DISABLED
        !result.serverReference.present
        !result.responseInformation.present
        !result.assignedClientIdentifier.present
        !result.sessionPresent
    cleanup:
        client.disconnect().join()
  }

  def "client should connect to broker with user and pass using MQTT 3.1.1"() {
    given:
        def client = buildExternalMqtt311Client()
    when:
        def result = connectWith(client, 'user1', 'password')
    then:
        result.returnCode == Mqtt3ConnAckReturnCode.SUCCESS
        !result.sessionPresent
    cleanup:
        client.disconnect().join()
  }

  def "client should connect to broker with user and pass using MQTT 5"() {
    given:
        def client = buildExternalMqtt5Client()
    when:
        def result = connectWith(client, 'user1', 'password')
    then:
        result.reasonCode == Mqtt5ConnAckReasonCode.SUCCESS
        !result.sessionExpiryInterval.present
        result.serverKeepAlive.present
        result.serverKeepAlive.getAsInt() == MqttProperties.SERVER_KEEP_ALIVE_DISABLED
        !result.serverReference.present
        !result.responseInformation.present
        !result.assignedClientIdentifier.present
        !result.sessionPresent
    cleanup:
        client.disconnect().join()
  }

  def "client should not connect to broker without providing a client id using MQTT 3.1.1"() {
    given:
        def client = buildExternalMqtt311Client("")
    when:
        client.connect().join()
    then:
        def ex = thrown CompletionException
        def cause = ex.cause as Mqtt3ConnAckException
        cause.mqttMessage.returnCode == Mqtt3ConnAckReturnCode.IDENTIFIER_REJECTED
  }

  @Ignore("until finalizing clientId validation")
  def "client should connect to broker without providing a client id using MQTT 5"() {
    given:
        def client = buildExternalMqtt5Client("")
    when:
        def result = client.connect().join()
    then:
        result.reasonCode == Mqtt5ConnAckReasonCode.SUCCESS
        result.assignedClientIdentifier.present
        result.assignedClientIdentifier.get().toString() != ""
    cleanup:
        client.disconnect().join()
  }

  def "client should not connect to broker with invalid client id using MQTT 3.1.1"(String clientId) {
    given:
        def client = buildExternalMqtt311Client(clientId)
    when:
        client.connect().join()
    then:
        def ex = thrown CompletionException
        def cause = ex.cause as Mqtt3ConnAckException
        cause.mqttMessage.returnCode == Mqtt3ConnAckReturnCode.IDENTIFIER_REJECTED
    where:
        clientId << ["!@#!@*()^&"]
  }

  def "client should not connect to broker with invalid client id using MQTT 5"(String clientId) {
    given:
        def client = buildExternalMqtt5Client(clientId)
    when:
        client.connect().join()
    then:
        def ex = thrown CompletionException
        def cause = ex.cause as Mqtt5ConnAckException
        cause.mqttMessage.reasonCode == Mqtt5ConnAckReasonCode.CLIENT_IDENTIFIER_NOT_VALID
    where:
        clientId << ["!@#!@*()^&"]
  }

  def "client should not connect to broker with wrong pass using MQTT 3.1.1"() {
    given:
        def client = buildExternalMqtt311Client()
    when:
        connectWith(client, "user", "wrongPassword")
    then:
        def ex = thrown CompletionException
        def cause = ex.cause as Mqtt3ConnAckException
        cause.mqttMessage.returnCode == Mqtt3ConnAckReturnCode.BAD_USER_NAME_OR_PASSWORD
  }

  def "client should not connect to broker with wrong pass using mqtt 5"() {
    given:
        def client = buildExternalMqtt5Client()
    when:
        connectWith(client, "user", "wrongPassword")
    then:
        def ex = thrown CompletionException
        def cause = ex.cause as Mqtt5ConnAckException
        cause.mqttMessage.reasonCode == Mqtt5ConnAckReasonCode.BAD_USER_NAME_OR_PASSWORD
  }

  def "mqtt 3.1.1 client should be generate session with one pending QoS 1 packet"() {
    given:
        def deviceId = generateClientId("device")
        def serviceId = generateClientId("service")
        def serviceName = "PublishRetryTest1"
        def publisher = buildExternalMqtt5Client(deviceId)
        def subscriber = buildMqtt311MockClient()
    when:
        publisher.connect().join()
        subscriber.connect()
        subscriber.send(new ConnectMqtt311OutMessage(serviceId, keepAlive))
    then:
        with(subscriber.readNext() as ConnectAckMqttInMessage) {
          reasonCode() == ConnectAckReasonCode.SUCCESS
        }
    when:
        subscriber.send(new SubscribeMqtt311OutMessage(
            1,
            Array.of(Subscription.minimal(TopicFilter.valueOf("service/$serviceName/device/+"), QoS.AT_LEAST_ONCE))))
    then:
        with(subscriber.readNext() as SubscribeAckMqttInMessage) {
          reasonCodes()
              .stream()
              .allMatch({ it == SubscribeAckReasonCode.GRANTED_QOS_1 })
        }
    when:
        publisher
            .publishWith()
            .topic("service/$serviceName/device/$deviceId")
            .qos(MqttQos.AT_MOST_ONCE)
            .payload(publishPayload)
            .send()
            .join()
    then:
        def receivedPublish = subscriber.readNext() as PublishMqttInMessage
        with(receivedPublish) {
          payload() == publishPayload
        }
    when:
        subscriber.disconnect()
        Thread.sleep(1_000)
        subscriber.connect()
        subscriber.send(new ConnectMqtt311OutMessage(serviceId, keepAlive))
    then:
        with(subscriber.readNext() as ConnectAckMqttInMessage) {
          reasonCode() == ConnectAckReasonCode.SUCCESS
        }
        with(subscriber.readNext() as PublishMqttInMessage) {
          duplicate()
          messageId() == receivedPublish.messageId()
          payload() == publishPayload
        }
    cleanup:
        subscriber.close()
        publisher.disconnect().join()
  }

  def "mqtt 5 client should be generate session with one pending QoS 1 packet"() {
    given:
        def deviceId = generateClientId("device")
        def serviceId = generateClientId("service")
        def serviceName = "PublishRetryTest2"
        def publisher = buildExternalMqtt5Client(deviceId)
        def subscriber = buildMqtt5MockClient()
    when:
        publisher.connect().join()
        subscriber.connect()
        subscriber.send(new ConnectMqtt5OutMessage(serviceId, keepAlive, 120))
    then:
        with(subscriber.readNext() as ConnectAckMqttInMessage) {
          reasonCode() == ConnectAckReasonCode.SUCCESS
        }
    when:
        subscriber.send(new SubscribeMqtt5OutMessage(
            1,
            Array.of(Subscription.minimal(TopicFilter.valueOf("service/$serviceName/device/+"), QoS.AT_LEAST_ONCE))))
    then:
        with(subscriber.readNext() as SubscribeAckMqttInMessage) {
          reasonCodes()
              .stream()
              .allMatch({ it == SubscribeAckReasonCode.GRANTED_QOS_1 })
        }
    when:
        publisher
            .publishWith()
            .topic("service/$serviceName/device/$deviceId")
            .qos(MqttQos.AT_MOST_ONCE)
            .payload(publishPayload)
            .send()
            .join()
    then:
        def receivedPublish = subscriber.readNext() as PublishMqttInMessage
        with(receivedPublish) {
          payload() == publishPayload
        }
    when:
        subscriber.disconnect()
        subscriber.connect()
        subscriber.send(new ConnectMqtt5OutMessage(serviceId, keepAlive, 120))
    then:
        with(subscriber.readNext() as ConnectAckMqttInMessage) {
          reasonCode() == ConnectAckReasonCode.SUCCESS
        }
        with(subscriber.readNext() as PublishMqttInMessage) {
          duplicate()
          messageId() == receivedPublish.messageId()
          payload() == publishPayload
        }
    cleanup:
        subscriber.close()
        publisher.disconnect().join()
  }

  def "mqtt 3.1.1 client should be generate session with one pending QoS 2 packet"() {
    given:
        def deviceId = generateClientId("device")
        def serviceId = generateClientId("service")
        def serviceName = "PublishRetryTest3"
        def publisher = buildExternalMqtt5Client(deviceId)
        def subscriber = buildMqtt311MockClient()
    when:
        publisher.connect().join()
        subscriber.connect()
        subscriber.send(new ConnectMqtt311OutMessage(serviceId, keepAlive))
    then:
        with(subscriber.readNext() as ConnectAckMqttInMessage) {
          reasonCode() == ConnectAckReasonCode.SUCCESS
        }
    when:
        subscriber.send(new SubscribeMqtt311OutMessage(
            1,
            Array.of(Subscription.minimal(TopicFilter.valueOf("service/$serviceName/device/+"), QoS.EXACTLY_ONCE))))
    then:
        with(subscriber.readNext() as SubscribeAckMqttInMessage) {
          reasonCodes()
              .stream()
              .allMatch({ it == SubscribeAckReasonCode.GRANTED_QOS_2 })
        }
    when:
        publisher
            .publishWith()
            .topic("service/$serviceName/device/$deviceId")
            .qos(MqttQos.AT_MOST_ONCE)
            .payload(publishPayload)
            .send()
            .join()
    then:
        def receivedPublish = subscriber.readNext() as PublishMqttInMessage
        with(receivedPublish) {
          payload() == publishPayload
        }
    when:
        subscriber.disconnect()
        subscriber.connect()
        subscriber.send(new ConnectMqtt311OutMessage(serviceId, keepAlive))
    then:
        with(subscriber.readNext() as ConnectAckMqttInMessage) {
          reasonCode() == ConnectAckReasonCode.SUCCESS
        }
        with(subscriber.readNext() as PublishMqttInMessage) {
          duplicate()
          messageId() == receivedPublish.messageId()
          payload() == publishPayload
        }
    when:
        subscriber.disconnect()
        subscriber.connect()
        subscriber.send(new ConnectMqtt311OutMessage(serviceId, keepAlive))
        subscriber.send(new PublishReceivedMqtt311OutMessage(receivedPublish.messageId()))
        subscriber.send(new PublishCompleteMqtt311OutMessage(receivedPublish.messageId()))
    then:
        with(subscriber.readNext() as ConnectAckMqttInMessage) {
          reasonCode() == ConnectAckReasonCode.SUCCESS
        }
        with(subscriber.readNext() as PublishMqttInMessage) {
          duplicate()
          messageId() == receivedPublish.messageId()
          payload() == publishPayload
        }
        with(subscriber.readNext() as PublishReleaseMqttInMessage) {
          messageId() == receivedPublish.messageId()
        }
    cleanup:
        subscriber.close()
        publisher.disconnect().join()
  }

  def "mqtt 5 client should be generate session with one pending QoS 2 packet"() {
    given:
        def deviceId = generateClientId("device")
        def serviceId = generateClientId("service")
        def serviceName = "PublishRetryTest4"
        def publisher = buildExternalMqtt5Client(deviceId)
        def subscriber = buildMqtt5MockClient()
    when:
        publisher.connect().join()
        subscriber.connect()
        subscriber.send(new ConnectMqtt5OutMessage(serviceId, keepAlive, 120))
    then:
        with(subscriber.readNext() as ConnectAckMqttInMessage) {
          reasonCode() == ConnectAckReasonCode.SUCCESS
        }
    when:
        subscriber.send(new SubscribeMqtt5OutMessage(
            1,
            Array.of(Subscription.minimal(TopicFilter.valueOf("service/$serviceName/device/+"), QoS.EXACTLY_ONCE))))
    then:
        with(subscriber.readNext() as SubscribeAckMqttInMessage) {
          reasonCodes()
              .stream()
              .allMatch({ it == SubscribeAckReasonCode.GRANTED_QOS_2 })
        }
    when:
        publisher
            .publishWith()
            .topic("service/$serviceName/device/$deviceId")
            .qos(MqttQos.AT_MOST_ONCE)
            .payload(publishPayload)
            .send()
            .join()
    then:
        def receivedPublish = subscriber.readNext() as PublishMqttInMessage
        with(receivedPublish) {
          payload() == publishPayload
        }
    when:
        subscriber.disconnect()
        subscriber.connect()
        subscriber.send(new ConnectMqtt5OutMessage(serviceId, keepAlive, 120))
    then:
        with(subscriber.readNext() as ConnectAckMqttInMessage) {
          reasonCode() == ConnectAckReasonCode.SUCCESS
        }
        with(subscriber.readNext() as PublishMqttInMessage) {
          duplicate()
          messageId() == receivedPublish.messageId()
          payload() == publishPayload
        }
    when:
        subscriber.disconnect()
        subscriber.connect()
        subscriber.send(new ConnectMqtt5OutMessage(serviceId, keepAlive, 120))
        subscriber.send(new PublishReceivedMqtt5OutMessage(
            receivedPublish.messageId(),
            PublishReceivedReasonCode.SUCCESS
        ))
        subscriber.send(new PublishCompleteMqtt5OutMessage(
            receivedPublish.messageId(),
            PublishCompletedReasonCode.SUCCESS
        ))
    then:
        with(subscriber.readNext() as ConnectAckMqttInMessage) {
          reasonCode() == ConnectAckReasonCode.SUCCESS
        }
        with(subscriber.readNext() as PublishMqttInMessage) {
          duplicate()
          messageId() == receivedPublish.messageId()
          payload() == publishPayload
        }
        with(subscriber.readNext() as PublishReleaseMqttInMessage) {
          messageId() == receivedPublish.messageId()
        }
    cleanup:
        subscriber.close()
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
