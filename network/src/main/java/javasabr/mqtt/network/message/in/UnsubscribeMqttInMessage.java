package javasabr.mqtt.network.message.in;

import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Set;
import javasabr.mqtt.base.util.DebugUtils;
import javasabr.mqtt.model.MqttMessageProperty;
import javasabr.mqtt.model.exception.MalformedMqttProtocolException;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.message.MqttMessageType;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.collections.array.ArrayFactory;
import javasabr.rlib.collections.array.MutableArray;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;

/**
 * Unsubscribe request.
 */
@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UnsubscribeMqttInMessage extends TrackableMqttInMessage {

  static {
    DebugUtils.registerIncludedFields("rawTopicFilters");
  }

  private static final byte MESSAGE_TYPE = (byte) MqttMessageType.UNSUBSCRIBE.ordinal();

  private static final Set<MqttMessageProperty> AVAILABLE_PROPERTIES = EnumSet.of(
      /*
        The User Property is allowed to appear multiple times to represent multiple name, value pairs. The same
        name is allowed to appear more than once.
       */
      MqttMessageProperty.USER_PROPERTY);

  @Nullable
  MutableArray<String> rawTopicFilters;

  public UnsubscribeMqttInMessage(byte info) {
    super(info);
  }

  @Override
  public byte messageType() {
    return MESSAGE_TYPE;
  }

  @Override
  protected void readPayload(MqttConnection connection, ByteBuffer buffer) {
    if (buffer.remaining() < 1) {
      throw new MalformedMqttProtocolException("No any topic filters.");
    }
    rawTopicFilters = ArrayFactory.mutableArray(String.class);
    while (buffer.hasRemaining()) {
      rawTopicFilters.add(readString(buffer, Integer.MAX_VALUE));
    }
  }

  public Array<String> rawTopicFilters() {
    return rawTopicFilters == null ? EMPTY_STRINGS : rawTopicFilters;
  }

  @Override
  protected Set<MqttMessageProperty> availableProperties() {
    return AVAILABLE_PROPERTIES;
  }
}
