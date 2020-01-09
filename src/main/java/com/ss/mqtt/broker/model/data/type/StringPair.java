package com.ss.mqtt.broker.model.data.type;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class StringPair {

    private final @NotNull String name;
    private final @NotNull String value;
}
