package javasabr.mqtt.model;

import java.util.stream.Stream;
import javasabr.mqtt.model.data.type.MqttDataType;
import javasabr.rlib.common.util.ClassUtils;
import javasabr.rlib.common.util.ObjectUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;

@Accessors(fluent = true, chain = false)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum MqttMessageProperty {
  PAYLOAD_FORMAT_INDICATOR(0x01, MqttDataType.BYTE),
  MESSAGE_EXPIRY_INTERVAL(0x02, MqttDataType.INTEGER),
  CONTENT_TYPE(0x03, MqttDataType.UTF_8_STRING),
  RESPONSE_TOPIC(0x08, MqttDataType.UTF_8_STRING),
  CORRELATION_DATA(0x09, MqttDataType.BINARY),
  SUBSCRIPTION_IDENTIFIER(0x0B, MqttDataType.MULTI_BYTE_INTEGER),
  SESSION_EXPIRY_INTERVAL(0x11, MqttDataType.INTEGER),
  ASSIGNED_CLIENT_IDENTIFIER(0x12, MqttDataType.UTF_8_STRING),
  SERVER_KEEP_ALIVE(0x13, MqttDataType.SHORT),
  AUTHENTICATION_METHOD(0x15, MqttDataType.UTF_8_STRING),
  AUTHENTICATION_DATA(0x16, MqttDataType.BINARY),
  REQUEST_PROBLEM_INFORMATION(0x17, MqttDataType.BYTE),
  WILL_DELAY_INTERVAL(0x18, MqttDataType.INTEGER),
  REQUEST_RESPONSE_INFORMATION(0x19, MqttDataType.BYTE),
  RESPONSE_INFORMATION(0x1A, MqttDataType.UTF_8_STRING),
  SERVER_REFERENCE(0x1C, MqttDataType.UTF_8_STRING),
  REASON_STRING(0x1F, MqttDataType.UTF_8_STRING),
  RECEIVE_MAXIMUM_PUBLISHES(0x21, MqttDataType.SHORT),
  TOPIC_ALIAS_MAXIMUM(0x22, MqttDataType.SHORT),
  TOPIC_ALIAS(0x23, MqttDataType.SHORT),
  MAXIMUM_QOS(0x24, MqttDataType.BYTE),
  RETAIN_AVAILABLE(0x25, MqttDataType.BYTE),
  USER_PROPERTY(0x26, MqttDataType.UTF_8_STRING_PAIR),
  MAXIMUM_MESSAGE_SIZE(0x27, MqttDataType.INTEGER),
  WILDCARD_SUBSCRIPTION_AVAILABLE(0x28, MqttDataType.BYTE),
  SUBSCRIPTION_IDENTIFIER_AVAILABLE(0x29, MqttDataType.BYTE),
  SHARED_SUBSCRIPTION_AVAILABLE(0x2A, MqttDataType.BYTE);

  private static final MqttMessageProperty[] PROPERTIES;

  static {

    int maxId = Stream
        .of(values())
        .mapToInt(MqttMessageProperty::id)
        .max()
        .orElse(0);

    var result = new MqttMessageProperty[maxId + 1];

    Stream
        .of(values())
        .forEach(prop -> result[prop.id] = prop);

    PROPERTIES = result;
  }

  public static MqttMessageProperty byId(int id) {
    if (id < 0 || id >= PROPERTIES.length) {
      throw new IllegalArgumentException("Unknown property with id: " + id);
    } else {
      return PROPERTIES[id];
    }
  }

  @Getter
  byte id;
  @Getter
  MqttDataType dataType;

  @Nullable
  Object defaultValue;

  MqttMessageProperty(int id, MqttDataType dataType) {
    this(id, dataType, null);
  }

  MqttMessageProperty(int id, MqttDataType dataType, @Nullable Object defaultValue) {
    this.id = (byte) id;
    this.dataType = dataType;
    this.defaultValue = defaultValue;
  }

  public <T> T defaultValue() {
    return ClassUtils.unsafeNNCast(ObjectUtils.notNull(defaultValue));
  }
}
