package com.ss.mqtt.broker.util;

import org.jetbrains.annotations.NotNull;

public class TopicUtils {

    public static boolean check(@NotNull String topic) {
        return topic.length() != 0 && !topic.contains("//") && !topic.startsWith("/") && !topic.endsWith("/");
    }
}
