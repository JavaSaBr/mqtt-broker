package javasabr.mqtt.application.extension

import javasabr.mqtt.model.PacketProperty
import javasabr.mqtt.model.data.type.PacketDataType
import javasabr.mqtt.model.data.type.StringPair
import javasabr.mqtt.network.MqttConnection
import javasabr.mqtt.network.packet.out.MqttWritablePacket
import javasabr.mqtt.network.utils.MqttDataUtils
import javasabr.rlib.collections.array.Array
import javasabr.rlib.collections.array.IntArray
import spock.lang.Specification

import java.nio.ByteBuffer

class SpecificationExtensions extends Specification {

  static final writer = new MqttWritablePacket() {

    @Override
    protected void writeImpl(MqttConnection connection, ByteBuffer buffer) {}
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

  static ByteBuffer putBytes(ByteBuffer self, byte[] value) {
    writer.writeBytes(self, value)
    return self
  }

  static ByteBuffer putProperty(ByteBuffer self, PacketProperty property, Array<?> values) {

    switch (property.dataType()) {
      case PacketDataType.UTF_8_STRING_PAIR: {
        writer.writeStringPairProperties(self, property, values as Array<StringPair>)
        break
      }
      default: {
        throw new IllegalStateException()
      }
    }

    return self
  }

  static ByteBuffer putProperty(ByteBuffer self, PacketProperty property, IntArray values) {
    values.each { writer.writeProperty(self, property, it) }
    return self
  }

  static ByteBuffer putBoolean(ByteBuffer self, boolean value) {
    self.put((value ? 1 : 0) as byte)
    return self
  }
}
