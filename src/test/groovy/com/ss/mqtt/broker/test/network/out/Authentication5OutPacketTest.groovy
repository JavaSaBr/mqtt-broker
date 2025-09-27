package com.ss.mqtt.broker.test.network.out

import javasabr.mqtt.legacy.model.reason.code.AuthenticateReasonCode
import javasabr.mqtt.legacy.network.packet.in.AuthenticationInPacket
import javasabr.mqtt.legacy.network.packet.out.Authentication5OutPacket
import javasabr.rlib.common.util.BufferUtils

class Authentication5OutPacketTest extends BaseOutPacketTest {

  def "should write packet correctly"() {

    given:

        def packet = new Authentication5OutPacket(
            userProperties,
            AuthenticateReasonCode.CONTINUE_AUTHENTICATION,
            reasonString,
            authMethod,
            authData,
        )

    when:

        def dataBuffer = BufferUtils.prepareBuffer(512) {
          packet.write(it)
        }

        def reader = new AuthenticationInPacket(0b1111_0000 as byte)
        def result = reader.read(mqtt5Connection, dataBuffer, dataBuffer.limit())

    then:
        result
        reader.reasonCode == AuthenticateReasonCode.CONTINUE_AUTHENTICATION
        reader.authenticationMethod == authMethod
        reader.authenticationData == authData
        reader.reason == reasonString
        reader.userProperties == userProperties
  }
}
