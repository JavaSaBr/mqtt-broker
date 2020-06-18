package com.ss.mqtt.broker.util;

import com.ss.mqtt.broker.model.SharedSubscriber;
import com.ss.mqtt.broker.model.SingleSubscriber;
import com.ss.mqtt.broker.model.Subscriber;
import com.ss.mqtt.broker.network.client.MqttClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SubscriberUtils {

    private static boolean isSharedSubscriber(@NotNull Subscriber subscriber) {
        return subscriber instanceof SharedSubscriber;
    }

    private static boolean isSingleSubscriber(@NotNull Subscriber subscriber) {
        return subscriber instanceof SingleSubscriber;
    }

    public static boolean isSharedSubscriberWithGroup(@NotNull String group, @NotNull Subscriber subscriber) {
        return isSharedSubscriber(subscriber) && group.equals(((SharedSubscriber) subscriber).getGroup());
    }

    public static @Nullable MqttClient singleSubscriberToMqttClient(@NotNull Subscriber subscriber) {
        return isSingleSubscriber(subscriber) ? ((SingleSubscriber) subscriber).getMqttClient() : null;
    }
}
