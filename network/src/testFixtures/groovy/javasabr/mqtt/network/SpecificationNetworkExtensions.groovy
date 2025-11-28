package javasabr.mqtt.network

import javasabr.mqtt.model.MqttMessageProperty
import javasabr.mqtt.model.data.type.MqttDataType
import javasabr.mqtt.model.data.type.StringPair
import javasabr.mqtt.model.message.MqttMessageType
import javasabr.mqtt.model.reason.code.ReasonCode
import javasabr.mqtt.network.message.out.MqttOutMessage
import javasabr.mqtt.network.util.MqttDataUtils
import javasabr.rlib.collections.array.Array
import javasabr.rlib.collections.array.IntArray
import spock.lang.Specification

import java.nio.ByteBuffer

class SpecificationNetworkExtensions extends Specification {

  static final writer = new MqttOutMessage() {

    @Override
    protected void writeImpl(MqttConnection connection, ByteBuffer buffer) {}

    @Override
    MqttMessageType messageType() {
      return MqttMessageType.PUBLISH
    }
  }

  static ByteBuffer putByte(ByteBuffer self, int value) {
    self.put((byte) value)
    return self
  }

  static ByteBuffer putMbi(ByteBuffer self, int value) {
    MqttDataUtils.writeMbi(value, self)
    return self
  }

  static ByteBuffer putProperty(ByteBuffer self, MqttMessageProperty property, boolean value) {
    return putProperty(self, property, value ? 1 : 0)
  }

  static ByteBuffer putProperty(ByteBuffer self, MqttMessageProperty property, long value) {
    writer.writeProperty(self, property, value)
    return self
  }

  static ByteBuffer putProperty(ByteBuffer self, MqttMessageProperty property, byte[] value) {
    writer.writeProperty(self, property, value)
    return self
  }

  static ByteBuffer putProperty(ByteBuffer self, MqttMessageProperty property, String value) {
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

  static ByteBuffer put(ByteBuffer self, ReasonCode reasonCode) {
    self.put((byte) reasonCode.code())
    return self
  }

  static ByteBuffer putProperty(ByteBuffer self, MqttMessageProperty property, Array<?> values) {

    switch (property.dataType()) {
      case MqttDataType.UTF_8_STRING_PAIR: {
        writer.writeStringPairProperties(self, property, values as Array<StringPair>)
        break
      }
      default: {
        throw new IllegalStateException()
      }
    }

    return self
  }

  static ByteBuffer putProperty(ByteBuffer self, MqttMessageProperty property, IntArray values) {
    values.each { writer.writeProperty(self, property, it) }
    return self
  }

  static ByteBuffer putBoolean(ByteBuffer self, boolean value) {
    self.put((value ? 1 : 0) as byte)
    return self
  }
}
