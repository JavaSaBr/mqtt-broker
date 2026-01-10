package javasabr.mqtt.auth.api.database;

import java.time.Duration;

public record DatabasePoolProperties(Duration maxIdleTime, int initialSize, int maxSize) {}

