package javasabr.mqtt.service.message.handler.impl

import javasabr.mqtt.model.MqttProperties
import javasabr.mqtt.model.MqttVersion
import javasabr.mqtt.model.reason.code.ConnectAckReasonCode
import javasabr.mqtt.network.message.in.ConnectMqttInMessage
import javasabr.mqtt.network.message.out.ConnectAckMqtt311OutMessage
import javasabr.mqtt.network.message.out.ConnectAckMqtt5OutMessage
import javasabr.mqtt.service.IntegrationServiceSpecification
import javasabr.mqtt.service.TestExternalNetworkMqttUser

import java.time.Duration

class ConnectInMqttInMessageHandlerTest extends IntegrationServiceSpecification {

  def "should configure default connection for MQTT 3.1.1"() {
    given:
        def serverConfig = defaultExternalServerConnectionConfig
        def testClientId = "ConnectInMqttInMessageHandlerTest_1"
        def testKeepAliveTime = 45
        def mqttConnection = initMockedExternalConnection(serverConfig)
        def messageHandler = new ConnectInMqttInMessageHandler(
            defaultClientIdRegistry,
            defaultAuthenticationService,
            defaultSessionService,
            defaultSubscriptionService,
            defaultMessageOutFactoryService)
        def mqttUser = mqttConnection.user() as TestExternalNetworkMqttUser
    when:
        def connectMessage = new ConnectMqttInMessage(ConnectMqttInMessage.MESSAGE_FLAGS) {{
          mqttVersion = MqttVersion.MQTT_3_1_1
          clientId = testClientId
          keepAlive = testKeepAliveTime
          sessionExpiryInterval = MqttProperties.SESSION_EXPIRY_INTERVAL_INFINITY
        }}
        messageHandler.processValidMessage(mqttConnection, connectMessage)
    then:
        def connectAck = mqttUser.nextSentMessage(ConnectAckMqtt311OutMessage)
        connectAck.reasonCode() == ConnectAckReasonCode.SUCCESS
        !connectAck.sessionPresent()
    then:
        mqttUser.clientId() == testClientId
        with(mqttUser.connectionConfig()) {
          mqttVersion() == MqttVersion.MQTT_3_1_1
          keepAlive() == testKeepAliveTime
          receiveMaxPublishes() == serverConfig.receiveMaxPublishes()
          maxMessageSize() == serverConfig.maxMessageSize()
          topicAliasMaxValue() == MqttProperties.TOPIC_ALIAS_MAX_DISABLED
        }
        with(mqttUser.session()) {
          clientId() == testClientId
          expiryInterval() == MqttProperties.SESSION_EXPIRY_DURATION_INFINITY
        }
  }

  def "should configure default connection for MQTT 5.0"() {
    given:
        def testMinKeepAliveTime = 120
        def serverConfig = defaultExternalServerConnectionConfig
            .withMinKeepAliveTime(testMinKeepAliveTime)
        def testClientId = "ConnectInMqttInMessageHandlerTest_2"
        def testKeepAliveTime = 45
        def mqttConnection = initMockedExternalConnection(serverConfig)
        def messageHandler = new ConnectInMqttInMessageHandler(
            defaultClientIdRegistry,
            defaultAuthenticationService,
            defaultSessionService,
            defaultSubscriptionService,
            defaultMessageOutFactoryService)
        def mqttUser = mqttConnection.user() as TestExternalNetworkMqttUser
    when:
        def connectMessage = new ConnectMqttInMessage(ConnectMqttInMessage.MESSAGE_FLAGS) {{
          mqttVersion = MqttVersion.MQTT_5
          clientId = testClientId
          keepAlive = testKeepAliveTime
        }}
        messageHandler.processValidMessage(mqttConnection, connectMessage)
    then:
        with(mqttUser.nextSentMessage(ConnectAckMqtt5OutMessage)) {
          reasonCode() == ConnectAckReasonCode.SUCCESS
          sessionPresent() == false
          requestedClientId() == testClientId
          requestedSessionExpiryInterval() == MqttProperties.SESSION_EXPIRY_INTERVAL_DISABLED
          requestedKeepAlive() == testKeepAliveTime
          requestedReceiveMaxPublishes() == MqttProperties.RECEIVE_MAX_PUBLISHES_IS_NOT_SET
        }
    then:
        mqttUser.clientId() == testClientId
        with(mqttUser.connectionConfig()) {
          mqttVersion() == MqttVersion.MQTT_5
          keepAlive() == testMinKeepAliveTime
          receiveMaxPublishes() == serverConfig.receiveMaxPublishes()
          maxMessageSize() == serverConfig.maxMessageSize()
          topicAliasMaxValue() == MqttProperties.TOPIC_ALIAS_MAX_DISABLED
        }
        with(mqttUser.session()) {
          clientId() == testClientId
          expiryInterval() == MqttProperties.SESSION_EXPIRY_DURATION_DISABLED
        }
  }
  
  def "should configure connection based on properties for MQTT 5.0"() {
    given:
        def serverConfig = defaultExternalServerConnectionConfig
            .withTopicAliasMaxValue(MqttProperties.TOPIC_ALIAS_MAX_MAX)
        def testClientId = "ConnectInMqttInMessageHandlerTest_3"
        def mqttConnection = initMockedExternalConnection(serverConfig)
        def messageHandler = new ConnectInMqttInMessageHandler(
            defaultClientIdRegistry,
            defaultAuthenticationService,
            defaultSessionService,
            defaultSubscriptionService,
            defaultMessageOutFactoryService)
        def mqttUser = mqttConnection.user() as TestExternalNetworkMqttUser
        def testKeepAliveTime = 45
        def testReceiveMaxPublishes = 112
        def testMaxMessageSize = 2048
        def testSessionExpiryInterval = 360
        def testTopicAliasMaxValue = 512
    when:
        def connectMessage = new ConnectMqttInMessage(ConnectMqttInMessage.MESSAGE_FLAGS) {{
          mqttVersion = MqttVersion.MQTT_5
          clientId = testClientId
          keepAlive = testKeepAliveTime
          receiveMaxPublishes = testReceiveMaxPublishes
          maxMessageSize = testMaxMessageSize
          topicAliasMaxValue = testTopicAliasMaxValue
          sessionExpiryInterval = testSessionExpiryInterval
        }}
        messageHandler.processValidMessage(mqttConnection, connectMessage)
    then:
        def connectAck = mqttUser.nextSentMessage(ConnectAckMqtt5OutMessage)
        connectAck.reasonCode() == ConnectAckReasonCode.SUCCESS
        !connectAck.sessionPresent()
    then:
        mqttUser.clientId() == testClientId
        with(mqttUser.connectionConfig()) {
          mqttVersion() == MqttVersion.MQTT_5
          keepAlive() == testKeepAliveTime
          receiveMaxPublishes() == testReceiveMaxPublishes
          maxMessageSize() == testMaxMessageSize
          topicAliasMaxValue() == testTopicAliasMaxValue
        }
        with(mqttUser.session()) {
          clientId() == testClientId
          expiryInterval() == Duration.ofSeconds(testSessionExpiryInterval)
        }
  }
}
