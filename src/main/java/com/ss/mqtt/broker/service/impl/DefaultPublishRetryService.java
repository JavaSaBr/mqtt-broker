package com.ss.mqtt.broker.service.impl;

import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.mqtt.broker.service.PublishRetryService;
import com.ss.rlib.common.concurrent.util.ThreadUtils;
import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayFactory;
import com.ss.rlib.common.util.array.ConcurrentArray;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;

public class DefaultPublishRetryService implements PublishRetryService, Closeable {

    private final @NotNull ConcurrentArray<MqttClient> registeredClients;
    private final @NotNull Thread checkThread;

    private final int checkInterval;
    private final int retryInterval;

    private volatile boolean closed;

    public DefaultPublishRetryService(int checkInterval, int retryInterval) {
        this.checkInterval = checkInterval;
        this.retryInterval = retryInterval;
        this.registeredClients = ConcurrentArray.ofType(MqttClient.class);
        this.checkThread = new Thread(this::checkPendingPackets, "DefaultPublishRetryService-Check");
        this.checkThread.setDaemon(true);
        this.checkThread.start();
    }

    @Override
    public void register(@NotNull MqttClient client) {
        registeredClients.runInWriteLock(client, Array::add);
    }

    @Override
    public void unregister(@NotNull MqttClient client) {
        registeredClients.runInWriteLock(client, Array::fastRemove);
    }

    private void checkPendingPackets() {

        var toCheck = ArrayFactory.newArray(MqttClient.class);
        var toUnregister = ArrayFactory.newArray(MqttClient.class);

        while (!closed) {

            toCheck.clear();
            toUnregister.clear();

            ThreadUtils.sleep(checkInterval);

            registeredClients.runInReadLock(toCheck, Array::copyTo);

            for (var client : toCheck) {

                var session = client.getSession();

                // if session is null it means that client was closed
                if (session == null) {
                    toUnregister.add(client);
                    continue;
                }

                session.removeExpiredPackets();
                session.resendPendingPacketsAsync(client, retryInterval);
            }

            if (toUnregister.isEmpty()) {
                return;
            }

            // unregister closed clients
            registeredClients.runInWriteLock(toUnregister, Array::fastRemoveAll);
        }
    }

    @Override
    public void close() {
        closed = true;
        checkThread.interrupt();
    }
}
