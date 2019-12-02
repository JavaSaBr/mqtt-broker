package com.ss.mqtt.broker.test.integration

import com.hivemq.client.mqtt.datatypes.MqttQos
import com.ss.mqtt.broker.model.QoS
import com.ss.mqtt.broker.model.SubscribeTopicFilter
import com.ss.mqtt.broker.model.reason.code.ConnectAckReasonCode
import com.ss.mqtt.broker.model.reason.code.PublishCompletedReasonCode
import com.ss.mqtt.broker.model.reason.code.PublishReceivedReasonCode
import com.ss.mqtt.broker.model.reason.code.SubscribeAckReasonCode
import com.ss.mqtt.broker.network.packet.in.ConnectAckInPacket
import com.ss.mqtt.broker.network.packet.in.PublishInPacket
import com.ss.mqtt.broker.network.packet.in.PublishReleaseInPacket
import com.ss.mqtt.broker.network.packet.in.SubscribeAckInPacket
import com.ss.mqtt.broker.network.packet.out.Connect311OutPacket
import com.ss.mqtt.broker.network.packet.out.Connect5OutPacket
import com.ss.mqtt.broker.network.packet.out.PublishComplete311OutPacket
import com.ss.mqtt.broker.network.packet.out.PublishComplete5OutPacket
import com.ss.mqtt.broker.network.packet.out.PublishReceived311OutPacket
import com.ss.mqtt.broker.network.packet.out.PublishReceived5OutPacket
import com.ss.mqtt.broker.network.packet.out.Subscribe311OutPacket
import com.ss.mqtt.broker.network.packet.out.Subscribe5OutPacket
import com.ss.mqtt.broker.service.MqttSessionService
import com.ss.rlib.common.util.array.Array
import org.springframework.beans.factory.annotation.Autowired

class PublishRetryTest extends IntegrationSpecification {
    
    @Autowired
    MqttSessionService mqttSessionService
    
    def "mqtt 3.1.1 client should be generate session with one pending QoS 1 packet"() {
        given:
            def publisher = buildMqtt5Client()
            def subscriber = buildMqtt311MockClient()
            def subscriberId = generateClientId()
        when:
            
            publisher.connect().join()
        
            subscriber.connect()
            subscriber.send(new Connect311OutPacket(subscriberId, keepAlive))
        
            def connectAck = subscriber.readNext() as ConnectAckInPacket
        
        then:
            connectAck.reasonCode == ConnectAckReasonCode.SUCCESS
        when:
           
            subscriber.send(new Subscribe311OutPacket(
                Array.of(new SubscribeTopicFilter("test/retry/$subscriberId", QoS.AT_LEAST_ONCE)),
                1
            ))
        
            def subscribeAck = subscriber.readNext() as SubscribeAckInPacket
        
        then:
            subscribeAck.reasonCodes.stream()
                .allMatch({ it == SubscribeAckReasonCode.GRANTED_QOS_1 })
        when:
           
            publisher.publishWith()
                .topic("test/retry/$subscriberId")
                .qos(MqttQos.AT_MOST_ONCE)
                .payload(publishPayload)
                .send()
                .join()
        
            def receivedPublish = subscriber.readNext() as PublishInPacket
        
        then:
            receivedPublish.payload == publishPayload
        when:
           
            subscriber.close()
        
            subscriber.connect()
            subscriber.send(new Connect311OutPacket(subscriberId, keepAlive))
        
            connectAck = subscriber.readNext() as ConnectAckInPacket
            def receivedDupPublish = subscriber.readNext() as PublishInPacket
        
        then:
            connectAck.reasonCode == ConnectAckReasonCode.SUCCESS
            receivedDupPublish.duplicate
            receivedDupPublish.packetId == receivedPublish.packetId
            receivedDupPublish.payload == publishPayload
        cleanup:
            subscriber.close()
            publisher.disconnect().join()
    }
    
    def "mqtt 5 client should be generate session with one pending QoS 1 packet"() {
        given:
            def publisher = buildMqtt5Client()
            def subscriber = buildMqtt5MockClient()
            def subscriberId = generateClientId()
        when:
            
            publisher.connect().join()
            
            subscriber.connect()
            subscriber.send(new Connect5OutPacket(subscriberId, keepAlive))
            
            def connectAck = subscriber.readNext() as ConnectAckInPacket
        
        then:
            connectAck.reasonCode == ConnectAckReasonCode.SUCCESS
        when:
            
            subscriber.send(new Subscribe5OutPacket(
                Array.of(new SubscribeTopicFilter("test/retry/$subscriberId", QoS.AT_LEAST_ONCE)),
                1
            ))
            
            def subscribeAck = subscriber.readNext() as SubscribeAckInPacket
        
        then:
            subscribeAck.reasonCodes.stream()
                .allMatch({ it == SubscribeAckReasonCode.GRANTED_QOS_1 })
        when:
            
            publisher.publishWith()
                .topic("test/retry/$subscriberId")
                .qos(MqttQos.AT_MOST_ONCE)
                .payload(publishPayload)
                .send()
                .join()
            
            def receivedPublish = subscriber.readNext() as PublishInPacket
        
        then:
            receivedPublish.payload == publishPayload
        when:
            
            subscriber.close()
            
            subscriber.connect()
            subscriber.send(new Connect5OutPacket(subscriberId, keepAlive))
            
            connectAck = subscriber.readNext() as ConnectAckInPacket
            def receivedDupPublish = subscriber.readNext() as PublishInPacket
        
        then:
            connectAck.reasonCode == ConnectAckReasonCode.SUCCESS
            receivedDupPublish.duplicate
            receivedDupPublish.packetId == receivedPublish.packetId
            receivedDupPublish.payload == publishPayload
        cleanup:
            subscriber.close()
            publisher.disconnect().join()
    }
    
    def "mqtt 3.1.1 client should be generate session with one pending QoS 2 packet"() {
        given:
            def publisher = buildMqtt5Client()
            def subscriber = buildMqtt311MockClient()
            def subscriberId = generateClientId()
        when:
            
            publisher.connect().join()
            
            subscriber.connect()
            subscriber.send(new Connect311OutPacket(subscriberId, keepAlive))
            
            def connectAck = subscriber.readNext() as ConnectAckInPacket
        
        then:
            connectAck.reasonCode == ConnectAckReasonCode.SUCCESS
        when:
            
            subscriber.send(new Subscribe311OutPacket(
                Array.of(new SubscribeTopicFilter("test/retry/$subscriberId", QoS.EXACTLY_ONCE)),
                1
            ))
            
            def subscribeAck = subscriber.readNext() as SubscribeAckInPacket
        
        then:
            subscribeAck.reasonCodes.stream()
                .allMatch({ it == SubscribeAckReasonCode.GRANTED_QOS_2 })
        when:
            
            publisher.publishWith()
                .topic("test/retry/$subscriberId")
                .qos(MqttQos.AT_MOST_ONCE)
                .payload(publishPayload)
                .send()
                .join()
            
            def receivedPublish = subscriber.readNext() as PublishInPacket
        
        then:
            receivedPublish.payload == publishPayload
        when:
            
            subscriber.close()
            
            subscriber.connect()
            subscriber.send(new Connect311OutPacket(subscriberId, keepAlive))
            
            connectAck = subscriber.readNext() as ConnectAckInPacket
            def receivedDupPublish = subscriber.readNext() as PublishInPacket
        
        then:
            connectAck.reasonCode == ConnectAckReasonCode.SUCCESS
            receivedDupPublish.duplicate
            receivedDupPublish.packetId == receivedPublish.packetId
            receivedDupPublish.payload == publishPayload
        when:
        
            subscriber.close()
        
            subscriber.connect()
            subscriber.send(new Connect311OutPacket(subscriberId, keepAlive))
        
            connectAck = subscriber.readNext() as ConnectAckInPacket
            receivedDupPublish = subscriber.readNext() as PublishInPacket
    
            subscriber.send(new PublishReceived311OutPacket(receivedDupPublish.getPacketId()))
            def releaseAck = subscriber.readNext() as PublishReleaseInPacket
        
            subscriber.send(new PublishComplete311OutPacket(receivedDupPublish.getPacketId()))
    
        then:
            connectAck.reasonCode == ConnectAckReasonCode.SUCCESS
            receivedDupPublish.duplicate
            receivedDupPublish.packetId == receivedPublish.packetId
            receivedDupPublish.payload == publishPayload
            releaseAck.packetId == receivedPublish.packetId
        cleanup:
            subscriber.close()
            publisher.disconnect().join()
    }
    
    def "mqtt 5 client should be generate session with one pending QoS 2 packet"() {
        given:
            def publisher = buildMqtt5Client()
            def subscriber = buildMqtt5MockClient()
            def subscriberId = generateClientId()
        when:
            
            publisher.connect().join()
            
            subscriber.connect()
            subscriber.send(new Connect5OutPacket(subscriberId, keepAlive))
            
            def connectAck = subscriber.readNext() as ConnectAckInPacket
        
        then:
            connectAck.reasonCode == ConnectAckReasonCode.SUCCESS
        when:
            
            subscriber.send(new Subscribe5OutPacket(
                Array.of(new SubscribeTopicFilter("test/retry/$subscriberId", QoS.EXACTLY_ONCE)),
                1
            ))
            
            def subscribeAck = subscriber.readNext() as SubscribeAckInPacket
        
        then:
            subscribeAck.reasonCodes.stream()
                .allMatch({ it == SubscribeAckReasonCode.GRANTED_QOS_2 })
        when:
            
            publisher.publishWith()
                .topic("test/retry/$subscriberId")
                .qos(MqttQos.AT_MOST_ONCE)
                .payload(publishPayload)
                .send()
                .join()
            
            def receivedPublish = subscriber.readNext() as PublishInPacket
        
        then:
            receivedPublish.payload == publishPayload
        when:
            
            subscriber.close()
            
            subscriber.connect()
            subscriber.send(new Connect5OutPacket(subscriberId, keepAlive))
            
            connectAck = subscriber.readNext() as ConnectAckInPacket
            def receivedDupPublish = subscriber.readNext() as PublishInPacket
        
        then:
            connectAck.reasonCode == ConnectAckReasonCode.SUCCESS
            receivedDupPublish.duplicate
            receivedDupPublish.packetId == receivedPublish.packetId
            receivedDupPublish.payload == publishPayload
        when:
        
            subscriber.close()
        
            subscriber.connect()
            subscriber.send(new Connect5OutPacket(subscriberId, keepAlive))
        
            connectAck = subscriber.readNext() as ConnectAckInPacket
            receivedDupPublish = subscriber.readNext() as PublishInPacket
        
            subscriber.send(new PublishReceived5OutPacket(
                receivedDupPublish.getPacketId(),
                PublishReceivedReasonCode.SUCCESS
            ))
        
            def releaseAck = subscriber.readNext() as PublishReleaseInPacket
        
            subscriber.send(new PublishComplete5OutPacket(
                receivedDupPublish.getPacketId(),
                PublishCompletedReasonCode.SUCCESS
            ))
    
        then:
            connectAck.reasonCode == ConnectAckReasonCode.SUCCESS
            receivedDupPublish.duplicate
            receivedDupPublish.packetId == receivedPublish.packetId
            receivedDupPublish.payload == publishPayload
            releaseAck.packetId == receivedPublish.packetId
        cleanup:
            subscriber.close()
            publisher.disconnect().join()
    }
}
