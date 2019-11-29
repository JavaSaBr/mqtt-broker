package com.ss.mqtt.broker.util;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ss.mqtt.broker.model.topic.AbstractTopic;
import com.ss.mqtt.broker.network.packet.in.*;
import com.ss.rlib.common.util.array.ArrayFactory;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class DebugUtils {

    private static class PrintOnlyProvidedFields implements ExclusionStrategy {

        private final @NotNull Class<?> type;
        private final @NotNull Set<String> fieldNames;

        public PrintOnlyProvidedFields(@NotNull Class<?> type, @NotNull String... fieldNames) {
            this.type = type;
            this.fieldNames = Set.of(fieldNames);
        }

        public PrintOnlyProvidedFields(@NotNull Class<?> type, @NotNull Set<String> fieldNames) {
            this.type = type;
            this.fieldNames = fieldNames;
        }

        @Override
        public boolean shouldSkipField(@NotNull FieldAttributes attributes) {

            var declaringClass = attributes.getDeclaringClass();

            if (declaringClass != type) {
                return false;
            } else {
                return !fieldNames.contains(attributes.getName());
            }
        }

        @Override
        public boolean shouldSkipClass(@NotNull Class<?> clazz) {
            return false;
        }
    }

    private static final ExclusionStrategy[] EXCLUSION_STRATEGIES = ArrayFactory.toArray(
        new PrintOnlyProvidedFields(MqttReadablePacket.class),
        new PrintOnlyProvidedFields(ConnectInPacket.class, "clientId", "keepAlive", "cleanStart"),
        new PrintOnlyProvidedFields(PublishInPacket.class, "topicName", "qos", "duplicate", "packetId"),
        new PrintOnlyProvidedFields(SubscribeInPacket.class, "packetId", "topicFilters"),
        new PrintOnlyProvidedFields(AbstractTopic.class, "rawTopic"),
        new PrintOnlyProvidedFields(DisconnectInPacket.class, "reasonCode"),
        new PrintOnlyProvidedFields(PublishAckInPacket.class, "reasonCode", "packetId"),
        new PrintOnlyProvidedFields(PublishReceivedInPacket.class, "reasonCode", "packetId"),
        new PrintOnlyProvidedFields(PublishReleaseInPacket.class, "reasonCode", "packetId"),
        new PrintOnlyProvidedFields(PublishCompleteInPacket.class, "reasonCode", "packetId")
    );

    private static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .setExclusionStrategies(EXCLUSION_STRATEGIES)
        .create();

    public static @NotNull String toJsonString(@NotNull Object object) {
        return GSON.toJson(object);
    }
}
