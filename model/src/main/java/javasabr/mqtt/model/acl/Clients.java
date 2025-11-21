package javasabr.mqtt.model.acl;

import java.util.List;

public record Clients(
    Operator operator,
    List<String> usernames,
    List<String> clientIds,
    List<String> clientAttrs,
    List<String> ipAddresses) {}
