package com.ss.mqtt.broker.model.impl;

import com.ss.mqtt.broker.model.MqttPropertyConstants;
import com.ss.mqtt.broker.model.MqttSession.UnsafeMqttSession;
import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.mqtt.broker.network.packet.HasPacketId;
import com.ss.mqtt.broker.network.packet.in.PublishInPacket;
import com.ss.rlib.common.util.ClassUtils;
import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ConcurrentArray;
import lombok.*;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

@Log4j2
@ToString(of = "clientId")
@EqualsAndHashCode(of = "clientId")
public class DefaultMqttSession implements UnsafeMqttSession {

    @AllArgsConstructor
    private static class PendingPublish {

        private final @NotNull PublishInPacket publish;
        private final @NotNull PendingPacketHandler handler;
        private final long registeredTime;
        private final int packetId;

        private volatile long lastAttemptTime;
    }

    private final @NotNull String clientId;
    private final @NotNull ConcurrentArray<PendingPublish> pendingPublishes;
    private final @NotNull AtomicInteger packetIdGenerator;

    private volatile @Getter @Setter long expirationTime = -1;

    public DefaultMqttSession(@NotNull String clientId) {
        this.clientId = clientId;
        this.pendingPublishes = ConcurrentArray.ofType(PendingPublish.class);
        this.packetIdGenerator = new AtomicInteger(0);
    }

    @Override
    public int nextPacketId() {

        var nextId = packetIdGenerator.incrementAndGet();

        if (nextId >= MqttPropertyConstants.MAXIMUM_PACKET_ID) {
            packetIdGenerator.compareAndSet(nextId, 0);
            return nextPacketId();
        }

        return nextId;
    }

    @Override
    public @NotNull String getClientId() {
        return clientId;
    }

    @Override
    public void registerPendingPublish(
        @NotNull PublishInPacket publish,
        @NotNull PendingPacketHandler handler,
        int packetId
    ) {

        var currentTime = System.currentTimeMillis();
        var pendingPublish = new PendingPublish(publish, handler, currentTime, packetId, currentTime);

        pendingPublishes.runInWriteLock(pendingPublish, Array::add);
    }

    @Override
    public void removeExpiredPackets() {

        if (pendingPublishes.isEmpty()) {
            return;
        }

        pendingPublishes.runInWriteLock(publishes -> {

            var currentTime = System.currentTimeMillis();
            var array = publishes.array();

            for (int i = 0, length = publishes.size(); i < length; i++) {

                var pendingPublish = array[i];

                var publish = pendingPublish.publish;
                var messageExpiryInterval = publish.getMessageExpiryInterval();

                if (messageExpiryInterval == MqttPropertyConstants.MESSAGE_EXPIRY_INTERVAL_UNDEFINED) {
                    continue;
                }

                var expiredTime = pendingPublish.registeredTime + (messageExpiryInterval * 1000);

                if (expiredTime < currentTime) {
                    publishes.fastRemove(i);
                    i--;
                    length--;
                }
            }
        });
    }

    @Override
    public void resendPendingPacketsAsync(@NotNull MqttClient client, int retryInterval) {
        var currentTime = System.currentTimeMillis();
        var stamp = pendingPublishes.readLock();
        try {
            for (var pendingPublish : pendingPublishes) {
                if (currentTime - pendingPublish.lastAttemptTime > retryInterval) {
                    pendingPublish.lastAttemptTime = currentTime;
                    pendingPublish.handler.retryAsync(client, pendingPublish.publish, pendingPublish.packetId);
                }
            }
        } finally {
            pendingPublishes.readUnlock(stamp);
        }
    }

    @Override
    public void unregisterPendingPacket(
        @NotNull MqttClient client,
        @NotNull HasPacketId response
    ) {

        var packetId = response.getPacketId();
        var pendingPublish = pendingPublishes.findAnyConvertedToIntInReadLock(
            packetId,
            pending -> pending.publish.getPacketId(),
            (id, targetId) -> id == targetId
        );

        if (pendingPublish == null) {
            log.warn("Not found pending publish for client {} by received packet {}", clientId, response);
            return;
        }

        var shouldBeRemoved = pendingPublish.handler.handleResponse(client, ClassUtils.unsafeNNCast(response));

        if (shouldBeRemoved) {
            pendingPublishes.runInWriteLock(pendingPublish, Array::fastRemove);
        }
    }

    @Override
    public void clear() {
        pendingPublishes.runInWriteLock(Collection::clear);
    }
}
