package com.ss.mqtt.broker.test.network.out

import com.ss.mqtt.broker.model.QoS
import com.ss.mqtt.broker.network.packet.in.ConnectInPacket
import com.ss.mqtt.broker.network.packet.out.Connect5OutPacket
import com.ss.rlib.common.util.ArrayUtils
import com.ss.rlib.common.util.BufferUtils

class Connect5OutPacketTest extends BaseOutPacketTest {
    
    def "should write packet correctly"() {
        
        given:
            
            def packet = new Connect5OutPacket(
                userName,
                "",
                clientId,
                userPassword,
                ArrayUtils.EMPTY_BYTE_ARRAY,
                QoS.AT_MOST_ONCE,
                keepAlive,
                willRetain,
                cleanStart,
                userProperties,
                authMethod,
                authData,
                sessionExpiryInterval,
                receiveMaximum,
                maximumPacketSize,
                topicAliasMaximum,
                requestResponseInformation,
                requestProblemInformation
            )
        
        when:
            
            def dataBuffer = BufferUtils.prepareBuffer(512) {
                packet.write(it)
            }
            
            def reader = new ConnectInPacket(0b0001_0000 as byte)
            def result = reader.read(mqtt5Connection, dataBuffer, dataBuffer.limit())
        
        then:
            result
            reader.username == userName
            reader.clientId == clientId
            reader.password == userPassword
            reader.keepAlive == keepAlive
            reader.userProperties == userProperties
            reader.cleanStart == cleanStart
            reader.willRetain == willRetain
            reader.authenticationMethod == authMethod
            reader.authenticationData == authData
            reader.sessionExpiryInterval == sessionExpiryInterval
            reader.receiveMax == receiveMaximum
            reader.maximumPacketSize == maximumPacketSize
            reader.topicAliasMaximum == topicAliasMaximum
            reader.requestResponseInformation == requestResponseInformation
            reader.requestProblemInformation == requestProblemInformation
    }
}
