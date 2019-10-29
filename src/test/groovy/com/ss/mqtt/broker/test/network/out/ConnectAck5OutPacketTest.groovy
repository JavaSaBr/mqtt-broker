package com.ss.mqtt.broker.test.network.out

import com.ss.mqtt.broker.model.ConnectAckReasonCode
import com.ss.mqtt.broker.model.MqttPropertyConstants
import com.ss.mqtt.broker.network.packet.in.ConnectAckInPacket
import com.ss.mqtt.broker.network.packet.out.ConnectAck5OutPacket
import com.ss.rlib.common.util.BufferUtils

class ConnectAck5OutPacketTest extends BaseOutPacketTest {
    
    def "should write packet correctly"() {
        
        given:
            
            def packet = new ConnectAck5OutPacket(
                mqttClient5,
                ConnectAckReasonCode.BAD_USER_NAME_OR_PASSWORD,
                sessionPresent,
                clientId,
                sessionExpiryInterval,
                keepAlive,
                reasonString,
                serverReference,
                responseInformation,
                authMethod,
                authData,
                userProperties
            )
        
        when:
            
            def dataBuffer = BufferUtils.prepareBuffer(512) {
                packet.write(it)
            }
            
            def reader = new ConnectAckInPacket(0b0010_0000 as byte)
            def result = reader.read(mqtt5Connection, dataBuffer, dataBuffer.limit())
        
        then:
            result
            reader.reasonCode == ConnectAckReasonCode.BAD_USER_NAME_OR_PASSWORD
            reader.sessionPresent == sessionPresent
            reader.retainAvailable == MqttPropertyConstants.RETAIN_AVAILABLE_DEFAULT
            reader.sessionExpiryInterval == sessionExpiryInterval
            reader.receiveMax == receiveMaximum
            reader.maximumPacketSize == maximumPacketSize
            reader.assignedClientId == clientId
            reader.topicAliasMaximum == topicAliasMaximum
            reader.reason == reasonString
            reader.userProperties == userProperties
            reader.wildcardSubscriptionAvailable == wildcardSubscriptionAvailable
            reader.subscriptionIdAvailable == subscriptionIdAvailable
            reader.sharedSubscriptionAvailable == sharedSubscriptionAvailable
            reader.serverKeepAlive == keepAlive
            reader.responseInformation == responseInformation
            reader.serverReference == serverReference
            reader.authenticationData == authData
            reader.authenticationMethod == authMethod
    }
}
