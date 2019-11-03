package com.ss.mqtt.broker.service;

import com.ss.mqtt.broker.network.packet.in.ConnectInPacket;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

public interface AuthenticationService {

    @NotNull Mono<String> auth(@NotNull ConnectInPacket packet);

}
