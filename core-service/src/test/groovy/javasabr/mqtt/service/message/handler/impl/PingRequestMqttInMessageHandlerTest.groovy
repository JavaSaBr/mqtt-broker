package javasabr.mqtt.service.message.handler.impl

import javasabr.mqtt.model.exception.MalformedProtocolMqttException
import javasabr.mqtt.model.message.MqttMessageType
import javasabr.mqtt.model.reason.code.PublishReceivedReasonCode
import javasabr.mqtt.network.MqttConnection
import javasabr.mqtt.network.NetworkUnitSpecification
import javasabr.mqtt.network.impl.ExternalNetworkMqttUser
import javasabr.mqtt.network.message.in.PingRequestMqttInMessage
import javasabr.mqtt.network.message.in.PingResponseMqttInMessage
import javasabr.mqtt.network.message.out.PingResponseMqtt311OutMessage
import javasabr.mqtt.service.MessageOutFactoryService
import javasabr.mqtt.service.message.out.factory.MqttMessageOutFactory
import javasabr.mqtt.service.session.impl.InMemoryNetworkMqttSession
import javasabr.rlib.common.util.BufferUtils

class PingRequestMqttInMessageHandlerTest extends NetworkUnitSpecification {

  def "should send PINGRESP in response to PINGREQ"() {
    given:
        def messageOutFactoryService = Mock(MessageOutFactoryService)
        def pintRequestHandler = new PingRequestMqttInMessageHandler(messageOutFactoryService)
    and:
        def session = Mock(InMemoryNetworkMqttSession)
        def user = Mock(ExternalNetworkMqttUser)
        def connection = Mock(MqttConnection)
        def pingResponse = Mock(PingResponseMqtt311OutMessage)
        def messageOutFactory = Mock(MqttMessageOutFactory)
        def pingRequest = new PingRequestMqttInMessage(PingResponseMqttInMessage.MESSAGE_FLAGS)

    when:
        pintRequestHandler.processValidMessage(connection, pingRequest)

    then:
        1 * messageOutFactory.newPingResponse() >> pingResponse
        1 * messageOutFactoryService.resolveFactory(user) >> messageOutFactory
        1 * connection.user() >> user
        1 * user.session() >> session
        1 * user.sendInBackground(_) >> { args ->
          assert args[0] instanceof PingResponseMqtt311OutMessage
        }
  }

  def "should not allow invalid message flags"() {
    given:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(testMessageId)
          it.put(PublishReceivedReasonCode.SUCCESS)
          it.putMbi(0)
        }
    when:
        def inMessage = new PingRequestMqttInMessage(0b0101_0101 as byte)
        def result = inMessage.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        !result
        with(inMessage) {
          exception() instanceof MalformedProtocolMqttException
          exception().message == "Unexpected message flags:[0b0101_0101] in message:[$MqttMessageType.PING_REQUEST]"
        }
  }
}
