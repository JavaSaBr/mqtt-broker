package com.ss.mqtt.broker.test.network.out

import com.ss.mqtt.broker.model.ConnectAckReasonCode
import com.ss.mqtt.broker.model.MqttPropertyConstants
import com.ss.mqtt.broker.network.packet.in.ConnectAckInPacket
import com.ss.mqtt.broker.network.packet.out.ConnectAck311OutPacket
import com.ss.rlib.common.util.ArrayUtils
import com.ss.rlib.common.util.BufferUtils
import com.ss.rlib.common.util.array.Array

class ConnectAck311OutPacketTest extends BaseOutPacketTest {
    
    def "should write packet correctly"() {
        
        given:
            
            def packet = new ConnectAck311OutPacket(
                mqtt311Client,
                ConnectAckReasonCode.BAD_USER_NAME_OR_PASSWORD,
                sessionPresent
            )
        
        when:
            
            def dataBuffer = BufferUtils.prepareBuffer(512) {
                packet.write(it)
            }
            
            def reader = new ConnectAckInPacket(0b0010_0000 as byte)
            def result = reader.read(mqtt311Connection, dataBuffer, dataBuffer.limit())
        
        then:
            result
            reader.reasonCode == ConnectAckReasonCode.BAD_USER_NAME_OR_PASSWORD
            reader.sessionPresent == sessionPresent
            reader.retainAvailable == MqttPropertyConstants.RETAIN_AVAILABLE_DEFAULT
            reader.sessionExpiryInterval == MqttPropertyConstants.SESSION_EXPIRY_INTERVAL_DEFAULT
            reader.receiveMax == MqttPropertyConstants.RECEIVE_MAXIMUM_DEFAULT
            reader.maximumPacketSize == MqttPropertyConstants.MAXIMUM_PACKET_SIZE_DEFAULT
            reader.assignedClientId == ""
            reader.topicAliasMaximum == MqttPropertyConstants.TOPIC_ALIAS_MAXIMUM_DEFAULT
            reader.reason == ""
            reader.userProperties == Array.empty()
            reader.wildcardSubscriptionAvailable == MqttPropertyConstants.WILDCARD_SUBSCRIPTION_AVAILABLE_DEFAULT
            reader.subscriptionIdAvailable == MqttPropertyConstants.SUBSCRIPTION_IDENTIFIER_AVAILABLE
            reader.sharedSubscriptionAvailable == MqttPropertyConstants.SHARED_SUBSCRIPTION_AVAILABLE_DEFAULT
            reader.serverKeepAlive == MqttPropertyConstants.SERVER_KEEP_ALIVE_UNDEFINED
            reader.responseInformation == ""
            reader.serverReference == ""
            reader.authenticationData == ArrayUtils.EMPTY_BYTE_ARRAY
            reader.authenticationMethod == ""
    }
}