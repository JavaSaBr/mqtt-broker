package com.ss.mqtt.broker.test.mock

import com.ss.mqtt.broker.network.MqttConnection
import com.ss.mqtt.broker.network.packet.PacketType
import com.ss.mqtt.broker.network.packet.in.ConnectAckInPacket
import com.ss.mqtt.broker.network.packet.in.ConnectInPacket
import com.ss.mqtt.broker.network.packet.in.MqttReadablePacket
import com.ss.mqtt.broker.network.packet.in.PublishInPacket
import com.ss.mqtt.broker.network.packet.in.PublishReleaseInPacket
import com.ss.mqtt.broker.network.packet.in.SubscribeAckInPacket
import com.ss.mqtt.broker.network.packet.out.MqttWritablePacket
import com.ss.mqtt.broker.util.MqttDataUtils
import com.ss.rlib.common.util.NumberUtils

import java.nio.ByteBuffer

class MqttMockClient {
    
    private final ByteBuffer received = ByteBuffer.allocate(1024).clear()
    
    private final String brokerHost
    private final int brokerPort
    private final MqttConnection connection
    
    private Socket socket
    
    MqttMockClient(String brokerHost, int brokerPort, MqttConnection connection) {
        this.brokerHost = brokerHost
        this.brokerPort = brokerPort
        this.connection = connection
    }
    
    void connect() {
        
        if (socket != null) {
            return
        }
        
        socket = new Socket(brokerHost, brokerPort)
    }
    
    void send(MqttWritablePacket packet) {
        
        def dataBuffer = ByteBuffer.allocate(1024)
        
        packet.write(dataBuffer)
        
        dataBuffer.flip()
    
        def finalBuffer = ByteBuffer.allocate(1024)
        finalBuffer.put((byte) packet.getPacketTypeAndFlags())
        
        MqttDataUtils.writeMbi(dataBuffer.remaining(), finalBuffer)
    
        finalBuffer.put(dataBuffer).flip()
        
        def out = socket.getOutputStream()
        out.write(finalBuffer.array(), 0, finalBuffer.remaining())
        
        Thread.sleep(50)
    }
    
    MqttReadablePacket readNext() {
    
        if (received.position() == 0) {
            
            def input = socket.getInputStream()
            def readBytes = input.read(received.array(), received.position(), received.capacity() - received.position())
    
            if (readBytes > 0) {
                received.position(received.position() + readBytes)
            }
        }
        
        received.flip()
        
        if (!received.hasRemaining()) {
            throw new IllegalStateException("No received bytes.")
        }
    
        def startByte = Byte.toUnsignedInt(received.get())
        def type = NumberUtils.getHighByteBits(startByte)
        def info = NumberUtils.getLowByteBits(startByte)
        def dataSize = MqttDataUtils.readMbi(received)
    
        MqttReadablePacket packet
    
        switch (PacketType.fromByte(type)) {
            case PacketType.CONNECT_ACK:
                packet = new ConnectAckInPacket(info)
                break
            case PacketType.SUBSCRIBE_ACK:
                packet = new SubscribeAckInPacket(info)
                break
            case PacketType.PUBLISH:
                packet = new PublishInPacket(info)
                break
            case PacketType.PUBLISH_RELEASED:
                packet = new PublishReleaseInPacket(info)
                break
            default:
                throw new IllegalStateException("Unknown packet of type: $type")
        }
        
        packet.read(connection, received, dataSize)
    
        if (received.hasRemaining()) {
            received.compact()
        } else {
            received.clear()
        }
        
        return packet
    }
    
    def close() {
    
        if (socket != null) {
            socket.close()
            socket = null
            received.clear()
            Thread.sleep(50)
        }
    }
}
