package com.ss.mqtt.broker.test.network.out

import com.ss.mqtt.broker.model.AuthenticateReasonCode
import com.ss.mqtt.broker.network.packet.in.AuthenticationInPacket
import com.ss.mqtt.broker.network.packet.out.Authentication5OutPacket
import com.ss.rlib.common.util.BufferUtils

class Authentication5OutPacketTest extends BaseOutPacketTest {
    
    def "should write packet correctly"() {
        
        given:
            
            def packet = new Authentication5OutPacket(
                mqtt5Client,
                AuthenticateReasonCode.CONTINUE_AUTHENTICATION,
                authMethod,
                authData,
                userProperties,
                reasonString
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
