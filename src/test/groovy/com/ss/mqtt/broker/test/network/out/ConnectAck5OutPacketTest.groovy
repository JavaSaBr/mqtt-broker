package com.ss.mqtt.broker.test.network.out

import com.ss.mqtt.broker.model.reason.code.ConnectAckReasonCode
import com.ss.mqtt.broker.model.MqttPropertyConstants
import com.ss.mqtt.broker.network.packet.in.ConnectAckInPacket
import com.ss.mqtt.broker.network.packet.out.ConnectAck5OutPacket
import com.ss.rlib.common.util.BufferUtils

class ConnectAck5OutPacketTest extends BaseOutPacketTest {
    
    def "should write packet correctly"() {
        
        given:
            
            def packet = new ConnectAck5OutPacket(
                mqtt5Client,
                ConnectAckReasonCode.BAD_USER_NAME_OR_PASSWORD,
                sessionPresent,
                "-1",
                MqttPropertyConstants.SESSION_EXPIRY_INTERVAL_UNDEFINED,
                MqttPropertyConstants.SERVER_KEEP_ALIVE_UNDEFINED,
                MqttPropertyConstants.TOPIC_ALIAS_MAXIMUM_UNDEFINED,
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
            reader.retainAvailable == retainAvailable
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
            reader.serverKeepAlive == serverKeepAlive
            reader.responseInformation == responseInformation
            reader.serverReference == serverReference
            reader.authenticationData == authData
            reader.authenticationMethod == authMethod
    }
}
