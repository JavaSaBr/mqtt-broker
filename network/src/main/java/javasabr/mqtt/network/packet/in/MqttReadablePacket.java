package javasabr.mqtt.network.packet.in;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;
import javasabr.mqtt.base.utils.DebugUtils;
import javasabr.mqtt.model.MqttVersion;
import javasabr.mqtt.model.PacketProperty;
import javasabr.mqtt.model.data.type.StringPair;
import javasabr.mqtt.model.exception.ConnectionRejectException;
import javasabr.mqtt.model.exception.MalformedPacketMqttException;
import javasabr.mqtt.model.exception.MqttException;
import javasabr.mqtt.model.reason.code.ConnectAckReasonCode;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.utils.MqttDataUtils;
import javasabr.rlib.collections.array.MutableArray;
import javasabr.rlib.common.util.ArrayUtils;
import javasabr.rlib.network.packet.impl.AbstractReadableNetworkPacket;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

public abstract class MqttReadablePacket extends AbstractReadableNetworkPacket<MqttConnection> {

  static {
    DebugUtils.registerIncludedFields("userProperties");
  }

  private static final MutableArray<StringPair> EMPTY_PROPERTIES = MutableArray.ofType(StringPair.class);

  @Getter
  @RequiredArgsConstructor
  private static class Utf8Decoder {
    private final CharsetDecoder decoder;
    private final ByteBuffer inBuffer;
    private final CharBuffer outBuffer;
  }

  private static final ThreadLocal<Utf8Decoder> LOCAL_DECODER = ThreadLocal.withInitial(() -> {

    var decoder = StandardCharsets.UTF_8
        .newDecoder()
        .onMalformedInput(CodingErrorAction.REPORT)
        .onUnmappableCharacter(CodingErrorAction.REPORT);

    return new Utf8Decoder(decoder, ByteBuffer.allocate(1024), CharBuffer.allocate(1024));
  });

  /**
   * The list of user properties.
   */
  @Getter
  protected MutableArray<StringPair> userProperties;

  /**
   * The happened exception during parsing this packet.
   */
  @Getter
  @Nullable
  protected Exception exception;

  protected MqttReadablePacket(byte info) {
    this.userProperties = EMPTY_PROPERTIES;
  }

  public abstract byte packetType();

  @Override
  protected void readImpl(MqttConnection connection, ByteBuffer buffer) {
    readVariableHeader(connection, buffer);

    if (isPropertiesSupported(connection, buffer)) {
      readProperties(buffer);
    }

    readPayload(connection, buffer);
  }

  @Override
  protected void handleException(MqttConnection connection, ByteBuffer buffer, Exception exception) {
    super.handleException(connection, buffer, exception);

    if (!(exception instanceof MqttException)) {
      exception = new ConnectionRejectException(exception, ConnectAckReasonCode.PROTOCOL_ERROR);
    }

    this.exception = exception;
  }

  protected boolean isPropertiesSupported(MqttConnection connection, ByteBuffer buffer) {
    return connection.isSupported(MqttVersion.MQTT_5);
  }

  protected void readVariableHeader(MqttConnection connection, ByteBuffer buffer) {
  }

  protected void readProperties(ByteBuffer buffer) {
    readProperties(buffer, getAvailableProperties());
  }

  protected void readPayload(MqttConnection connection, ByteBuffer buffer) {
  }

  protected void readProperties(ByteBuffer buffer, Set<PacketProperty> availableProperties) {

    var propertiesLength = MqttDataUtils.readMbi(buffer);

    if (propertiesLength == -1) {
      throw new IllegalStateException("Can't read properties length.");
    } else if (propertiesLength == 0) {
      return;
    }

    var lastPositionInBuffer = buffer.position() + propertiesLength;

    while (buffer.position() < lastPositionInBuffer) {

      var property = PacketProperty.of(readUnsignedByte(buffer));

      if (!availableProperties.contains(property)) {
        throw new IllegalStateException("Property: " + property + " is not available for this packet.");
      }

      switch (property.getDataType()) {
        case BYTE:
          applyProperty(property, readUnsignedByte(buffer));
          break;
        case SHORT:
          applyProperty(property, readUnsignedShort(buffer));
          break;
        case INTEGER:
          applyProperty(property, readUnsignedInt(buffer));
          break;
        case MULTI_BYTE_INTEGER:
          applyProperty(property, MqttDataUtils.readMbi(buffer));
          break;
        case UTF_8_STRING:
          applyProperty(property, readString(buffer, Integer.MAX_VALUE));
          break;
        case UTF_8_STRING_PAIR:
          applyProperty(property, new StringPair(readString(buffer, Integer.MAX_VALUE), readString(buffer, Integer.MAX_VALUE)));
          break;
        case BINARY:
          applyProperty(property, readBytes(buffer));
          break;
        default:
          throw new IllegalArgumentException("Unsupported data type: " + property.getDataType());
      }
    }
  }

  protected Set<PacketProperty> getAvailableProperties() {
    return Collections.emptySet();
  }

  protected void applyProperty(PacketProperty property, long value) {
  }

  protected void applyProperty(PacketProperty property, String value) {
  }

  protected void applyProperty(PacketProperty property, byte[] value) {
  }

  protected void applyProperty(PacketProperty property, StringPair value) {
    switch (property) {
      case USER_PROPERTY:
        if (userProperties == EMPTY_PROPERTIES) {
          userProperties = MutableArray.ofType(StringPair.class);
        }
        userProperties.add(value);
        break;
    }
  }

  protected int readUnsignedByte(ByteBuffer buffer) {
    return Byte.toUnsignedInt(buffer.get());
  }

  protected int readUnsignedShort(ByteBuffer buffer) {
    return Short.toUnsignedInt(buffer.getShort());
  }

  protected long readUnsignedInt(ByteBuffer buffer) {
    return Integer.toUnsignedLong(buffer.getInt());
  }

  @Override
  protected String readString(ByteBuffer buffer, int maxLength) {

    var utf8Decoder = LOCAL_DECODER.get();
    var inBuffer = utf8Decoder.getInBuffer();

    var stringLength = readShort(buffer) & 0xFFFF;

    if (stringLength > inBuffer.capacity()) {
      throw new MalformedPacketMqttException();
    }

    var decoder = utf8Decoder.getDecoder();
    var outBuffer = utf8Decoder.getOutBuffer();

    buffer.get(
        inBuffer
            .clear()
            .array(), 0, stringLength);

    decoder.reset();

    var result = decoder.decode(
        inBuffer
            .position(stringLength)
            .flip(), outBuffer.clear(), true);

    if (result.isError()) {
      throw new MalformedPacketMqttException();
    }

    return new String(inBuffer.array(), 0, stringLength, StandardCharsets.UTF_8);
  }

  protected byte[] readBytes(ByteBuffer buffer) {
    var data = new byte[readShort(buffer) & 0xFFFF];
    buffer.get(data);
    return data;
  }

  protected byte[] readPayload(ByteBuffer buffer) {

    var payloadSize = buffer.limit() - buffer.position();

    if (payloadSize < 1) {
      return ArrayUtils.EMPTY_BYTE_ARRAY;
    }

    var data = new byte[payloadSize];
    buffer.get(data);
    return data;
  }

  protected void unexpectedProperty(PacketProperty property) {
    throw new IllegalArgumentException("Unsupported property: " + property);
  }

  @Override
  public String toString() {
    return DebugUtils.toJsonString(this);
  }
}
