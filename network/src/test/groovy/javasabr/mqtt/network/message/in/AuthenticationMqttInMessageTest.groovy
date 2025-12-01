package javasabr.mqtt.network.message.in

import javasabr.mqtt.model.MqttMessageProperty
import javasabr.mqtt.model.exception.MalformedProtocolMqttException
import javasabr.mqtt.model.message.MqttMessageType
import javasabr.mqtt.model.reason.code.AuthenticateReasonCode
import javasabr.rlib.common.util.BufferUtils

class AuthenticationMqttInMessageTest extends BaseMqttInMessageTest {

  def "should read message correctly as MQTT 5.0"() {
    given:
        def propertiesBuffer = BufferUtils.prepareBuffer(512) {
          it.putProperty(MqttMessageProperty.AUTHENTICATION_METHOD, authMethod)
          it.putProperty(MqttMessageProperty.AUTHENTICATION_DATA, authData)
          it.putProperty(MqttMessageProperty.REASON_STRING, reasonString)
          it.putProperty(MqttMessageProperty.USER_PROPERTY, userProperties)
        }
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.put(AuthenticateReasonCode.SUCCESS)
          it.putMbi(propertiesBuffer.limit())
          it.put(propertiesBuffer)
        }
    when:
        def inMessage = new AuthenticationMqttInMessage(AuthenticationMqttInMessage.MESSAGE_FLAGS)
        def result = inMessage.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        inMessage.reasonCode() == AuthenticateReasonCode.SUCCESS
        inMessage.authenticationMethod() == authMethod
        inMessage.authenticationData() == authData
        inMessage.reason() == reasonString
        inMessage.userProperties() == userProperties
    when:
        def propertiesBuffer2 = BufferUtils.prepareBuffer(512) {
          it.putProperty(MqttMessageProperty.AUTHENTICATION_METHOD, authMethod)
          it.putProperty(MqttMessageProperty.REASON_STRING, reasonString)
          it.putProperty(MqttMessageProperty.USER_PROPERTY, userProperties)
          it.putProperty(MqttMessageProperty.AUTHENTICATION_DATA, authData)
        }
        def dataBuffer2 = BufferUtils.prepareBuffer(512) {
          it.put(AuthenticateReasonCode.CONTINUE_AUTHENTICATION)
          it.putMbi(propertiesBuffer2.limit())
          it.put(propertiesBuffer2)
        }
        def inMessage2 = new AuthenticationMqttInMessage(AuthenticationMqttInMessage.MESSAGE_FLAGS)
        def result2 = inMessage2.read(defaultMqtt5Connection, dataBuffer2, dataBuffer2.limit())
    then:
        result2
        inMessage2.reasonCode() == AuthenticateReasonCode.CONTINUE_AUTHENTICATION
        inMessage2.authenticationMethod() == authMethod
        inMessage2.authenticationData() == authData
        inMessage2.reason() == reasonString
        inMessage2.userProperties() == userProperties
    when:
        def propertiesBuffer3 = BufferUtils.prepareBuffer(512) {
          it.putProperty(MqttMessageProperty.AUTHENTICATION_METHOD, authMethod)
          it.putProperty(MqttMessageProperty.AUTHENTICATION_DATA, authData)
        }
        def dataBuffer3 = BufferUtils.prepareBuffer(512) {
          it.put(AuthenticateReasonCode.CONTINUE_AUTHENTICATION)
          it.putMbi(propertiesBuffer3.limit())
          it.put(propertiesBuffer3)
        }
        def inMessage3 = new AuthenticationMqttInMessage(AuthenticationMqttInMessage.MESSAGE_FLAGS)
        def result3 = inMessage3.read(defaultMqtt5Connection, dataBuffer3, dataBuffer3.limit())
    then:
        result3
        inMessage3.reasonCode() == AuthenticateReasonCode.CONTINUE_AUTHENTICATION
        inMessage3.authenticationMethod() == authMethod
        inMessage3.authenticationData() == authData
        inMessage3.reason() == null
        inMessage3.userProperties() == MqttInMessage.EMPTY_USER_PROPERTIES
    when:
        def dataBuffer4 = BufferUtils.prepareBuffer(512) {
          it.put(AuthenticateReasonCode.SUCCESS)
          it.putMbi(0)
        }
        def inMessage4 = new AuthenticationMqttInMessage(AuthenticationMqttInMessage.MESSAGE_FLAGS)
        def result4 = inMessage4.read(defaultMqtt5Connection, dataBuffer3, dataBuffer3.limit())
    then:
        result4
        inMessage4.reasonCode() == AuthenticateReasonCode.SUCCESS
        inMessage4.authenticationMethod() == null
        inMessage4.authenticationData() == null
        inMessage4.reason() == null
        inMessage4.userProperties() == MqttInMessage.EMPTY_USER_PROPERTIES
  }

  def "should not allow to put auth method 2 times"() {
    given:
        def propertiesBuffer = BufferUtils.prepareBuffer(512) {
          it.putProperty(MqttMessageProperty.AUTHENTICATION_METHOD, "method1")
          it.putProperty(MqttMessageProperty.AUTHENTICATION_METHOD, "method2")
        }
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.put(AuthenticateReasonCode.SUCCESS)
          it.putMbi(propertiesBuffer.limit())
          it.put(propertiesBuffer)
        }
    when:
        def inMessage = new AuthenticationMqttInMessage(AuthenticationMqttInMessage.MESSAGE_FLAGS)
        def result = inMessage.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        !result
        inMessage.exception() instanceof MalformedProtocolMqttException
        inMessage.exception().message == "Property:[$MqttMessageProperty.AUTHENTICATION_METHOD] is already presented in message:[$MqttMessageType.AUTHENTICATION]"
  }

  def "should not allow to put auth data 2 times"() {
    given:
        def propertiesBuffer = BufferUtils.prepareBuffer(512) {
          it.putProperty(MqttMessageProperty.AUTHENTICATION_DATA, "data1".bytes)
          it.putProperty(MqttMessageProperty.AUTHENTICATION_DATA, "data2".bytes)
        }
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.put(AuthenticateReasonCode.SUCCESS)
          it.putMbi(propertiesBuffer.limit())
          it.put(propertiesBuffer)
        }
    when:
        def inMessage = new AuthenticationMqttInMessage(AuthenticationMqttInMessage.MESSAGE_FLAGS)
        def result = inMessage.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        !result
        inMessage.exception() instanceof MalformedProtocolMqttException
        inMessage.exception().message == "Property:[$MqttMessageProperty.AUTHENTICATION_DATA] is already presented in message:[$MqttMessageType.AUTHENTICATION]"
  }

  def "should not allow to put reason 2 times"() {
    given:
        def propertiesBuffer = BufferUtils.prepareBuffer(512) {
          it.putProperty(MqttMessageProperty.REASON_STRING, "reason1")
          it.putProperty(MqttMessageProperty.REASON_STRING, "reason2")
        }
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.put(AuthenticateReasonCode.SUCCESS)
          it.putMbi(propertiesBuffer.limit())
          it.put(propertiesBuffer)
        }
    when:
        def inMessage = new AuthenticationMqttInMessage(AuthenticationMqttInMessage.MESSAGE_FLAGS)
        def result = inMessage.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        !result
        inMessage.exception() instanceof MalformedProtocolMqttException
        inMessage.exception().message == "Property:[$MqttMessageProperty.REASON_STRING] is already presented in message:[$MqttMessageType.AUTHENTICATION]"
  }

  def "should not allow invalid message flags"() {
    given:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.put(AuthenticateReasonCode.SUCCESS)
          it.putMbi(0)
        }
    when:
        def inMessage = new AuthenticationMqttInMessage(0b0101_0101 as byte)
        def result = inMessage.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        !result
        inMessage.exception() instanceof MalformedProtocolMqttException
        inMessage.exception().message == "Unexpected message flags:[0b0101_0101] in message:[$MqttMessageType.AUTHENTICATION]"
  }
}
