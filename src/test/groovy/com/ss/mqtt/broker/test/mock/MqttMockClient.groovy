package com.ss.mqtt.broker.test.mock

import com.ss.mqtt.broker.network.packet.out.MqttWritablePacket
import com.ss.mqtt.broker.util.MqttDataUtils

import java.nio.ByteBuffer
import java.util.concurrent.Executors

class MqttMockClient {

    private static final scheduler = Executors.newSingleThreadScheduledExecutor()
    
    private final String brokerHost
    private final int brokerPort
    
    private Socket socket
    
    MqttMockClient(String brokerHost, int brokerPort) {
        this.brokerHost = brokerHost
        this.brokerPort = brokerPort
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
    }
}
