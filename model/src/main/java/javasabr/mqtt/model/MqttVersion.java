package javasabr.mqtt.model;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;

@Getter
@Accessors(fluent = true, chain = false)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum MqttVersion {
  UNKNOWN("Unknown", -1),
  MQTT_3_1_1("MQTT", 4),
  MQTT_5("MQTT", 5);

  private static final Map<String, @Nullable MqttVersion[]> NAME_LEVEL_VERSIONS;

  static {

    var map = new HashMap<String, @Nullable MqttVersion[]>();

    for (MqttVersion mqttVersion : values()) {
      if (mqttVersion.version < 0) {
        continue;
      }

      @Nullable MqttVersion[] versions = map.computeIfAbsent(mqttVersion.rawName(), _ -> new MqttVersion[mqttVersion.version + 1]);
      if (versions.length > mqttVersion.version()) {
        versions[mqttVersion.version()] = mqttVersion;
        continue;
      }

      versions = Arrays.copyOf(versions, mqttVersion.version + 1);
      versions[mqttVersion.version] = mqttVersion;

      map.replace(mqttVersion.rawName, versions);
    }

    NAME_LEVEL_VERSIONS = Map.copyOf(map);
  }

  byte[] nameInBytes;
  String rawName;
  byte version;

  MqttVersion(String rawName, int version) {
    this.rawName = rawName;
    this.version = (byte) version;
    this.nameInBytes = rawName.getBytes(StandardCharsets.UTF_8);
  }

  public boolean include(MqttVersion version) {
    return ordinal() >= version.ordinal();
  }

  public boolean isLowerThan(MqttVersion version) {
    return ordinal() < version.ordinal();
  }

  public boolean isEqualOrHigherThan(MqttVersion version) {
    return ordinal() >= version.ordinal();
  }
  
  public static MqttVersion of(String name, byte level) {

    if (level < 0) {
      return MqttVersion.UNKNOWN;
    }

    @Nullable MqttVersion[] availableVersions = NAME_LEVEL_VERSIONS.get(name);
    if (availableVersions == null) {
      return MqttVersion.UNKNOWN;
    } else if (availableVersions.length <= level || availableVersions[level] == null) {
      return MqttVersion.UNKNOWN;
    }

    //noinspection DataFlowIssue
    return availableVersions[level];
  }
}
