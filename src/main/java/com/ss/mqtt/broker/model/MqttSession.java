package com.ss.mqtt.broker.model;

import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.mqtt.broker.network.packet.HasPacketId;
import com.ss.mqtt.broker.network.packet.in.PublishInPacket;
import org.jetbrains.annotations.NotNull;

public interface MqttSession {

    interface UnsafeMqttSession extends MqttSession {

        void setExpirationTime(long expirationTime);

        void clear();
    }

    interface PendingPacketHandler {

        /**
         * @return true if pending packet can be removed.
         */
        boolean handleResponse(@NotNull MqttClient client, @NotNull HasPacketId response);

        default void retryAsync(@NotNull MqttClient client, @NotNull PublishInPacket packet, int packetId) {}
    }

    @NotNull String getClientId();

    int nextPacketId();

    /**
     * @return the expiration time in ms or -1 if it should not be expired now.
     */
    long getExpirationTime();

    void removeExpiredPackets();
    void resendPendingPacketsAsync(@NotNull MqttClient client, int retryInterval);

    boolean hasOutPending();
    boolean hasInPending();
    boolean hasInPending(int packetId);
    boolean hasOutPending(int packetId);

    void registerOutPublish(@NotNull PublishInPacket publish, @NotNull PendingPacketHandler handler, int packetId);
    void registerInPublish(@NotNull PublishInPacket publish, @NotNull PendingPacketHandler handler, int packetId);

    void updateOutPendingPacket(@NotNull MqttClient client, @NotNull HasPacketId response);
    void updateInPendingPacket(@NotNull MqttClient client, @NotNull HasPacketId response);
}
