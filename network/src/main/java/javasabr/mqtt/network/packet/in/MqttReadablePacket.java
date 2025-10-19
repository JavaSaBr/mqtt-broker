package javasabr.mqtt.network.packet.in;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;
import javasabr.mqtt.base.utils.DebugUtils;
import javasabr.mqtt.model.MqttServerConnectionConfig;
import javasabr.mqtt.model.MqttVersion;
import javasabr.mqtt.model.PacketProperty;
import javasabr.mqtt.model.data.type.StringPair;
import javasabr.mqtt.model.exception.ConnectionRejectException;
import javasabr.mqtt.model.exception.MalformedPacketMqttException;
import javasabr.mqtt.model.exception.MqttException;
import javasabr.mqtt.model.reason.code.ConnectAckReasonCode;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.utils.MqttDataUtils;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.collections.array.MutableArray;
import javasabr.rlib.common.util.ArrayUtils;
import javasabr.rlib.network.packet.impl.AbstractReadableNetworkPacket;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;

@Accessors(fluent = true, chain = false)
@FieldDefaults(level = AccessLevel.PROTECTED)
public abstract class MqttReadablePacket extends AbstractReadableNetworkPacket<MqttConnection> {

  static {
    DebugUtils.registerIncludedFields("userProperties");
  }

  private static final Array<StringPair> EMPTY_PROPERTIES = Array.empty(StringPair.class);

  private record Utf8Decoder(CharsetDecoder decoder, ByteBuffer inBuffer, CharBuffer outBuffer) {}

  private static final ThreadLocal<Utf8Decoder> LOCAL_DECODER = ThreadLocal.withInitial(() -> {

    var decoder = StandardCharsets.UTF_8
        .newDecoder()
        .onMalformedInput(CodingErrorAction.REPORT)
        .onUnmappableCharacter(CodingErrorAction.REPORT);

    return new Utf8Decoder(
        decoder,
        ByteBuffer.allocate(2048),
        CharBuffer.allocate(2048));
  });

  /**
   * The list of user properties.
   */
  @Nullable
  MutableArray<StringPair> userProperties;

  /**
   * The happened exception during parsing this packet.
   */
  @Getter
  @Nullable
  Exception exception;

  protected MqttReadablePacket(byte info) {}

  public abstract byte packetType();

  public Array<StringPair> userProperties() {
    return userProperties == null ? EMPTY_PROPERTIES : userProperties;
  }

  @Override
  protected void readImpl(MqttConnection connection, ByteBuffer buffer) {
    readVariableHeader(connection, buffer);

    if (isPropertiesSupported(connection, buffer)) {
      readProperties(connection, buffer);
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

  protected void readVariableHeader(MqttConnection connection, ByteBuffer buffer) {}

  protected void readProperties(MqttConnection connection, ByteBuffer buffer) {
    readProperties(connection, buffer, getAvailableProperties());
  }

  protected void readPayload(MqttConnection connection, ByteBuffer buffer) {}

  protected void readProperties(MqttConnection connection, ByteBuffer buffer, Set<PacketProperty> availableProperties) {

    int propertiesLength = MqttDataUtils.readMbi(buffer);
    if (propertiesLength == MqttDataUtils.UNKNOWN_LENGTH) {
      throw new IllegalStateException("Can't read properties length.");
    } else if (propertiesLength == 0) {
      return;
    }

    int lastPositionInBuffer = buffer.position() + propertiesLength;
    MqttServerConnectionConfig serverConnectionConfig = connection.serverConnectionConfig();

    while (buffer.position() < lastPositionInBuffer) {
      PacketProperty property = PacketProperty.byId(readByteUnsigned(buffer));
      if (!availableProperties.contains(property)) {
        throw new IllegalStateException("Property:[" + property + "] is not available for packet:[" + this + "]");
      }
      switch (property.dataType()) {
        case BYTE: {
          applyProperty(property, readByteUnsigned(buffer));
          break;
        }
        case SHORT: {
          applyProperty(property, readShortUnsigned(buffer));
          break;
        }
        case INTEGER: {
          applyProperty(property, readIntUnsigned(buffer));
          break;
        }
        case MULTI_BYTE_INTEGER: {
          applyProperty(property, MqttDataUtils.readMbi(buffer));
          break;
        }
        case UTF_8_STRING: {
          applyProperty(property, readString(buffer, serverConnectionConfig.maxStringLength()));
          break;
        }
        case UTF_8_STRING_PAIR: {
          String name = readString(buffer, serverConnectionConfig.maxStringLength());
          String value = readString(buffer, serverConnectionConfig.maxStringLength());
          applyProperty(property, new StringPair(name, value));
          break;
        }
        case BINARY: {
          applyProperty(property, readBytes(buffer, serverConnectionConfig.maxBinarySize()));
          break;
        }
        default: {
          throw new IllegalArgumentException("Unsupported data type: " + property.dataType());
        }
      }
    }
  }

  protected Set<PacketProperty> getAvailableProperties() {
    return Collections.emptySet();
  }

  protected void applyProperty(PacketProperty property, long value) {}

  protected void applyProperty(PacketProperty property, String value) {}

  protected void applyProperty(PacketProperty property, byte[] value) {}

  protected void applyProperty(PacketProperty property, StringPair value) {
    switch (property) {
      case USER_PROPERTY:
        if (userProperties == null) {
          userProperties = MutableArray.ofType(StringPair.class);
        }
        userProperties.add(value);
        break;
    }
  }

  @Override
  protected String readString(ByteBuffer buffer, int maxLength) {

    Utf8Decoder utf8Decoder = LOCAL_DECODER.get();
    ByteBuffer inBuffer = utf8Decoder.inBuffer();

    int stringLength = readShortUnsigned(buffer);
    if (stringLength > inBuffer.capacity() || stringLength > maxLength) {
      throw new MalformedPacketMqttException();
    }

    inBuffer.clear();
    buffer.get(inBuffer.array(), 0, stringLength);
    inBuffer
        .position(stringLength)
        .flip();

    CharBuffer outBuffer = utf8Decoder
        .outBuffer()
        .clear();

    CharsetDecoder decoder = utf8Decoder.decoder();
    decoder.reset();

    CoderResult result = decoder.decode(inBuffer, outBuffer, true);
    if (result.isError()) {
      throw new MalformedPacketMqttException();
    }

    return outBuffer
        .flip()
        .toString();
  }

  protected byte[] readBytes(ByteBuffer buffer, int maxLength) {
    int length = readShortUnsigned(buffer);
    if (length >= maxLength) {
      throw new IllegalStateException("Unexpected too many bytes:[" + length + ">" + maxLength + "]");
    }
    byte[] data = new byte[length];
    buffer.get(data);
    return data;
  }

  protected byte[] readPayload(ByteBuffer buffer) {

    int payloadSize = buffer.limit() - buffer.position();
    if (payloadSize < 1) {
      return ArrayUtils.EMPTY_BYTE_ARRAY;
    }

    var data = new byte[payloadSize];
    buffer.get(data);
    return data;
  }

  protected void unexpectedProperty(PacketProperty property) {
    throw new IllegalArgumentException("Unsupported property:[" + property + "]");
  }

  @Override
  public String toString() {
    return DebugUtils.toJsonString(this);
  }
}
