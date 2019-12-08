package com.ss.mqtt.broker.model;

import com.ss.mqtt.broker.model.topic.TopicFilter;
import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.mqtt.broker.network.packet.HasPacketId;
import com.ss.mqtt.broker.network.packet.in.PublishInPacket;
import com.ss.rlib.common.function.NotNullTripleConsumer;
import org.jetbrains.annotations.NotNull;

public interface MqttSession {

    interface UnsafeMqttSession extends MqttSession {

        void setExpirationTime(long expirationTime);

        void clear();

        void onPersisted();

        void onRestored();
    }

    interface PendingPacketHandler {

        /**
         * @return true if pending packet can be removed.
         */
        boolean handleResponse(@NotNull MqttClient client, @NotNull HasPacketId response);

        default void resend(@NotNull MqttClient client, @NotNull PublishInPacket packet, int packetId) {}
    }

    @NotNull String getClientId();

    int nextPacketId();

    /**
     * @return the expiration time in ms or -1 if it should not be expired now.
     */
    long getExpirationTime();

    void resendPendingPackets(@NotNull MqttClient client);

    boolean hasOutPending();
    boolean hasInPending();

    boolean hasInPending(int packetId);
    boolean hasOutPending(int packetId);

    void registerOutPublish(@NotNull PublishInPacket publish, @NotNull PendingPacketHandler handler, int packetId);
    void registerInPublish(@NotNull PublishInPacket publish, @NotNull PendingPacketHandler handler, int packetId);

    void updateOutPendingPacket(@NotNull MqttClient client, @NotNull HasPacketId response);
    void updateInPendingPacket(@NotNull MqttClient client, @NotNull HasPacketId response);

    <F, S> void forEachTopicFilter(
        @NotNull F first,
        @NotNull S second,
        @NotNull NotNullTripleConsumer<F, S, SubscribeTopicFilter> consumer
    );
    void addSubscriber(@NotNull SubscribeTopicFilter subscribe);
    void removeSubscriber(@NotNull TopicFilter subscribe);
}
