package com.ss.mqtt.broker;

import com.ss.mqtt.broker.config.MqttBrokerConfig;
import com.ss.rlib.common.concurrent.util.ConcurrentUtils;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Import({
    MqttBrokerConfig.class
})
@Configuration
@RequiredArgsConstructor
public class MqttBrokerApplication {

    public static void main(@NotNull String[] args) {
        ConcurrentUtils.wait(SpringApplication.run(MqttBrokerApplication.class, args));
    }
}
