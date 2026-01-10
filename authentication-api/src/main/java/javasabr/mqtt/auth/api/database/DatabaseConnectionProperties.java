package javasabr.mqtt.auth.api.database;

public record DatabaseConnectionProperties(DatabaseDriver driver, String host, int port, String dbName) {}

