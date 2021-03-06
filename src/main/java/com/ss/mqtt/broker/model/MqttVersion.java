package com.ss.mqtt.broker.model;

import com.ss.mqtt.broker.factory.packet.out.Mqtt311PacketOutFactory;
import com.ss.mqtt.broker.factory.packet.out.Mqtt5PacketOutFactory;
import com.ss.mqtt.broker.factory.packet.out.MqttPacketOutFactory;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Getter
public enum MqttVersion {
    UNKNOWN("Unknown", -1, new Mqtt311PacketOutFactory()),
    MQTT_3_1_1("MQTT", 4, new Mqtt311PacketOutFactory()),
    MQTT_5("MQTT", 5, new Mqtt5PacketOutFactory());

    private static final Map<String, MqttVersion[]> NAME_LEVEL_VERSIONS;

    static {

        var map = new HashMap<String, MqttVersion[]>();

        for (var mqttVersion : values()) {

            if (mqttVersion.version < 0) {
                continue;
            }

            var versions = map.computeIfAbsent(mqttVersion.name, name -> new MqttVersion[mqttVersion.version + 1]);

            if (versions.length > mqttVersion.version) {
                versions[mqttVersion.version] = mqttVersion;
                continue;
            }

            versions = Arrays.copyOf(versions, mqttVersion.version + 1);
            versions[mqttVersion.version] = mqttVersion;

            map.replace(mqttVersion.name, versions);
        }

        NAME_LEVEL_VERSIONS = Map.copyOf(map);
    }

    public static @NotNull MqttVersion of(@NotNull String name, byte level) {

        if (level < 0) {
            return MqttVersion.UNKNOWN;
        }

        var availableVersions = NAME_LEVEL_VERSIONS.get(name);

        if (availableVersions == null) {
            return MqttVersion.UNKNOWN;
        } else if (availableVersions.length <= level || availableVersions[level] == null) {
            return MqttVersion.UNKNOWN;
        }

        return availableVersions[level];
    }

    private final @NotNull MqttPacketOutFactory packetOutFactory;
    private final byte @NotNull [] nameInBytes;
    private final @NotNull String name;
    private final byte version;

    MqttVersion(@NotNull String name, int version, @NotNull MqttPacketOutFactory packetOutFactory) {
        this.name = name;
        this.version = (byte) version;
        this.nameInBytes = name.getBytes(StandardCharsets.UTF_8);
        this.packetOutFactory = packetOutFactory;
    }
}
