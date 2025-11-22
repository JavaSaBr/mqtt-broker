package javasabr.mqtt.network.message.in;

import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Set;
import javasabr.mqtt.base.util.DebugUtils;
import javasabr.mqtt.model.MqttClientConnectionConfig;
import javasabr.mqtt.model.MqttMessageProperty;
import javasabr.mqtt.model.MqttProtocolErrors;
import javasabr.mqtt.model.exception.MalformedProtocolMqttException;
import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.network.MqttConnection;
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
@FieldDefaults(level = AccessLevel.PROTECTED)
public class UnsubscribeMqttInMessage extends TrackableMqttInMessage {

  static {
    DebugUtils.registerIncludedFields("rawTopicFilters");
  }

  private static final byte MESSAGE_TYPE = (byte) MqttMessageType.UNSUBSCRIBE.ordinal();
  public static final byte MESSAGE_FLAGS = 0b0000_0010;

  private static final Set<MqttMessageProperty> AVAILABLE_PROPERTIES = EnumSet.of(
      /*
        The User Property is allowed to appear multiple times to represent multiple name, value pairs. The same
        name is allowed to appear more than once.
       */
      MqttMessageProperty.USER_PROPERTY);

  @Nullable
  MutableArray<String> rawTopicFilters;

  public UnsubscribeMqttInMessage(byte messageFlags) {
    super(messageFlags);
  }

  @Override
  public byte messageType() {
    return MESSAGE_TYPE;
  }

  @Override
  protected boolean validMessageFlags(byte messageFlags) {
    return messageFlags == 0b0000_0010;
  }

  @Override
  protected void readPayload(MqttConnection connection, ByteBuffer buffer) {
    if (!buffer.hasRemaining()) {
      throw new MalformedProtocolMqttException(MqttProtocolErrors.NO_ANY_TOPIC_FILTER);
    }

    MqttClientConnectionConfig connectionConfig = connection.clientConnectionConfig();
    int maxStringLength = connectionConfig.maxStringLength();

    rawTopicFilters = ArrayFactory.mutableArray(String.class);
    while (buffer.hasRemaining()) {
      rawTopicFilters.add(readString(buffer, maxStringLength));
    }
  }

  public Array<String> rawTopicFilters() {
    return rawTopicFilters == null ? EMPTY_STRINGS : rawTopicFilters;
  }

  public int topicFiltersCount() {
    return rawTopicFilters == null ? 0 : rawTopicFilters.size();
  }

  @Override
  protected Set<MqttMessageProperty> availableProperties() {
    return AVAILABLE_PROPERTIES;
  }
}
