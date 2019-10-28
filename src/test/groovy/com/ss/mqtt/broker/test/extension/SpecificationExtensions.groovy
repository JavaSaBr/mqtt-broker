package com.ss.mqtt.broker.test.extension

import com.ss.mqtt.broker.model.PacketDataType
import com.ss.mqtt.broker.model.PacketProperty
import com.ss.mqtt.broker.model.StringPair
import com.ss.mqtt.broker.network.packet.out.MqttWritablePacket
import com.ss.mqtt.broker.util.MqttDataUtils
import com.ss.rlib.common.util.array.Array
import com.ss.rlib.common.util.array.IntegerArray
import org.jetbrains.annotations.NotNull
import spock.lang.Specification

import java.nio.ByteBuffer

class SpecificationExtensions extends Specification {
    
    static final writer = new MqttWritablePacket(null) {
    
        @Override
        protected void writeImpl(@NotNull ByteBuffer buffer) {
        }
    }
    
    static ByteBuffer putMbi(ByteBuffer self, int value) {
        MqttDataUtils.writeMbi(value, self)
        return self
    }
    
    static ByteBuffer putProperty(ByteBuffer self, PacketProperty property, boolean value) {
        return putProperty(self, property, value ? 1 : 0)
    }
    
    static ByteBuffer putProperty(ByteBuffer self, PacketProperty property, long value) {
        writer.writeProperty(self, property, value)
        return self
    }
    
    static ByteBuffer putProperty(ByteBuffer self, PacketProperty property, byte[] value) {
        writer.writeProperty(self, property, value)
        return self
    }
    
    static ByteBuffer putProperty(ByteBuffer self, PacketProperty property, String value) {
        writer.writeProperty(self, property, value)
        return self
    }
    
    static ByteBuffer putString(ByteBuffer self, String value) {
        writer.writeString(self, value)
        return self
    }
    
    static ByteBuffer putProperty(ByteBuffer self, PacketProperty property, Array<?> values) {
       
        switch (property.getDataType()) {
            case PacketDataType.UTF_8_STRING_PAIR:
                writer.writeStringPairProperties(self, property, values as Array<StringPair>)
                break
            default:
                throw new IllegalStateException()
        }
        
        return self
    }
    
    static ByteBuffer putProperty(ByteBuffer self, PacketProperty property, IntegerArray values) {
        values.each { writer.writeProperty(self, property, it) }
        return self
    }
    
    static ByteBuffer putBoolean(ByteBuffer self, boolean value) {
        self.put((value ? 1 : 0) as byte)
        return self
    }
}
