package com.ss.mqtt.broker.model.impl;

import com.ss.mqtt.broker.model.MqttSession.UnsafeMqttSession;
import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.mqtt.broker.network.packet.HasPacketId;
import com.ss.mqtt.broker.network.packet.in.MqttReadablePacket;
import com.ss.mqtt.broker.network.packet.out.PublishOutPacket;
import com.ss.rlib.common.util.ClassUtils;
import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ConcurrentArray;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

@Log4j2
@ToString(of = "clientId")
public class DefaultMqttSession implements UnsafeMqttSession {

    @RequiredArgsConstructor
    private static class PendingPublish<T extends HasPacketId> {
        private final PublishOutPacket publish;
        private final PendingCallback<T> callback;
        private final long registeredTime;
    }

    private final @NotNull String clientId;
    private final @NotNull ConcurrentArray<PendingPublish<?>> pendingPublishes;

    private volatile @Getter @Setter long expirationTime = -1;

    public DefaultMqttSession(@NotNull String clientId) {
        this.clientId = clientId;
        this.pendingPublishes = ConcurrentArray.ofType(PendingPublish.class);
    }

    @Override
    public @NotNull String getClientId() {
        return clientId;
    }

    @Override
    public void registerPendingPublish(@NotNull PublishOutPacket publish) {
        pendingPublishes.runInWriteLock(publish, (array, packet) ->
            array.add(new PendingPublish<>(packet, PendingCallback.EMPTY, System.currentTimeMillis())));
    }

    @Override
    public <T extends MqttReadablePacket & HasPacketId> void registerPendingPublish(
        @NotNull PublishOutPacket publish,
        @NotNull PendingCallback<T> callback
    ) {
        // FIXME add new method to array
        var stamp = pendingPublishes.writeLock();
        try {
            pendingPublishes.add(new PendingPublish<>(publish, callback, System.currentTimeMillis()));
        } finally {
            pendingPublishes.writeUnlock(stamp);
        }
    }

    @Override
    public <T extends MqttReadablePacket & HasPacketId> void unregisterPendingPacket(
        @NotNull MqttClient client,
        @NotNull T feedback
    ) {

        var packetId = feedback.getPacketId();

        // FIXME add new method to array
        var pendingPublish = pendingPublishes.findAnyInReadLock(
            packetId,
            (id, pending) -> id == pending.publish.getPacketId()
        );

        if (pendingPublish == null) {
            log.warn("Not found pending publish for client {} by received packet {}", clientId, feedback);
            return;
        }

        var shouldBeRemoved = pendingPublish.callback.handle(client, ClassUtils.unsafeNNCast(feedback));

        if (shouldBeRemoved) {
            pendingPublishes.runInWriteLock(pendingPublish, Array::fastRemove);
        }
    }

    @Override
    public void clear() {
        pendingPublishes.runInWriteLock(Collection::clear);
    }
}
