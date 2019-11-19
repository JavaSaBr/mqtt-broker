package com.ss.mqtt.broker.model;

import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.mqtt.broker.network.packet.HasPacketId;
import com.ss.mqtt.broker.network.packet.in.MqttReadablePacket;
import com.ss.mqtt.broker.network.packet.out.PublishOutPacket;
import org.jetbrains.annotations.NotNull;

public interface MqttSession {

    interface UnsafeMqttSession extends MqttSession {

        void setExpirationTime(long expirationTime);

        void clear();
    }

    interface PendingCallback<T extends HasPacketId> {

        @NotNull PendingCallback<?> EMPTY = (client, feedback) -> true;

        /**
         * @return true of pending packet can be removed.
         */
        boolean handle(@NotNull MqttClient client, @NotNull T feedback);
    }

    @NotNull String getClientId();

    int nextPacketId();

    /**
     * @return the expiration time in ms or -1 if it should not be expired now.
     */
    long getExpirationTime();

    void registerPendingPublish(@NotNull PublishOutPacket publish);

    <T extends MqttReadablePacket & HasPacketId> void registerPendingPublish(
        @NotNull PublishOutPacket publish,
        @NotNull MqttSession.PendingCallback<T> callback
    );

    <T extends MqttReadablePacket & HasPacketId> void unregisterPendingPacket(
        @NotNull MqttClient client,
        @NotNull T feedback
    );
}
