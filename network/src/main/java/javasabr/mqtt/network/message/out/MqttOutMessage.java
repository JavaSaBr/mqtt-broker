package javasabr.mqtt.network.message.out;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import javasabr.mqtt.base.util.DebugUtils;
import javasabr.mqtt.model.PacketProperty;
import javasabr.mqtt.model.data.type.StringPair;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.util.MqttDataUtils;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.common.util.NumberUtils;
import javasabr.rlib.network.packet.impl.AbstractWritableNetworkPacket;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class MqttOutMessage extends AbstractWritableNetworkPacket<MqttConnection> {

  private static final ThreadLocal<ByteBuffer> LOCAL_BUFFER = ThreadLocal.withInitial(() -> ByteBuffer.allocate(
      1024 * 1024));

  protected static final int PACKET_ID_SIZE = 2;

  @Override
  protected void writeImpl(MqttConnection connection, ByteBuffer buffer) {
    writeVariableHeader(connection, buffer);

    if (isPropertiesSupported(connection)) {
      appendProperties(connection, buffer);
    }

    writePayload(connection, buffer);
  }

  protected void writeVariableHeader(MqttConnection connection, ByteBuffer buffer) {}

  protected void writePayload(MqttConnection connection, ByteBuffer buffer) {}

  protected boolean isPropertiesSupported(MqttConnection connection) {
    return false;
  }

  protected void writeProperties(MqttConnection connection, ByteBuffer buffer) {}

  public final int packetTypeAndFlags() {
    byte type = messageType();
    byte controlFlags = packetFlags();
    return NumberUtils.setHighByteBits(controlFlags, type);
  }

  protected byte messageType() {
    throw new UnsupportedOperationException();
  }

  protected byte packetFlags() {
    return 0;
  }

  protected ByteBuffer propertiesBuffer() {
    return LOCAL_BUFFER.get().clear();
  }

  private void appendProperties(MqttConnection connection, ByteBuffer buffer) {

    ByteBuffer propertiesBuffer = propertiesBuffer();
    writeProperties(connection, propertiesBuffer);

    if (propertiesBuffer.position() < 1) {
      buffer.put((byte) 0);
      return;
    }

    propertiesBuffer.flip();
    MqttDataUtils
        .writeMbi(propertiesBuffer.limit(), buffer)
        .put(propertiesBuffer);
  }

  public void writeProperty(ByteBuffer buffer, PacketProperty property, boolean value) {
    writeProperty(buffer, property, value ? 1 : 0);
  }

  public void writeProperty(ByteBuffer buffer, PacketProperty property, boolean value, boolean def) {
    if (value != def) {
      writeProperty(buffer, property, value ? 1 : 0);
    }
  }

  public void writeProperty(ByteBuffer buffer, PacketProperty property, long value, long def) {
    if (value != def) {
      writeProperty(buffer, property, value);
    }
  }

  public void writeProperty(ByteBuffer buffer, PacketProperty property, long value) {
    buffer.put(property.id());
    switch (property.dataType()) {
      case BYTE -> writeByte(buffer, (int) value);
      case SHORT -> writeShort(buffer, (int) value);
      case INTEGER -> writeInt(buffer, (int) value);
      case MULTI_BYTE_INTEGER -> writeMbi(buffer, (int) value);
      default -> throw new IllegalArgumentException("Incorrect property type: " + property);
    }
  }

  public void writeProperty(
      ByteBuffer buffer,
      PacketProperty property,
      String value,
      String def) {
    if (!def.equals(value)) {
      writeProperty(buffer, property, value);
    }
  }

  public void writeProperty(ByteBuffer buffer, PacketProperty property, StringPair value) {
    buffer.put(property.id());
    writeString(buffer, value.name());
    writeString(buffer, value.value());
  }

  public void writeNotEmptyProperty(ByteBuffer buffer, PacketProperty property, String value) {
    if (!value.isEmpty()) {
      writeProperty(buffer, property, value);
    }
  }

  public void writeNotEmptyProperty(ByteBuffer buffer, PacketProperty property, byte[] value) {
    if (value.length > 0) {
      writeProperty(buffer, property, value);
    }
  }

  public void writeProperty(ByteBuffer buffer, PacketProperty property, String value) {
    buffer.put(property.id());
    writeString(buffer, value);
  }

  public void writeProperty(ByteBuffer buffer, PacketProperty property, byte[] value) {
    buffer.put(property.id());
    writeBytes(buffer, value);
  }

  public void writeStringPairProperties(ByteBuffer buffer, PacketProperty property, Array<StringPair> pairs) {
    if (pairs.isEmpty()) {
      return;
    }
    for (StringPair pair : pairs) {
      buffer.put(property.id());
      writeStringPair(buffer, pair);
    }
  }

  @Override
  public void writeString(ByteBuffer buffer, String string) {
    var bytes = string.getBytes(StandardCharsets.UTF_8);
    buffer.putShort((short) bytes.length);
    buffer.put(bytes);
  }

  public void writeStringPair(ByteBuffer buffer, StringPair pair) {
    writeString(buffer, pair.name());
    writeString(buffer, pair.value());
  }

  public void writeMbi(ByteBuffer buffer, int value) {
    MqttDataUtils.writeMbi(value, buffer);
  }

  public void writeBytes(ByteBuffer buffer, byte[] bytes) {
    buffer.putShort((short) bytes.length);
    buffer.put(bytes);
  }

  @Override
  public String toString() {
    return DebugUtils.toJsonString(this);
  }
}
