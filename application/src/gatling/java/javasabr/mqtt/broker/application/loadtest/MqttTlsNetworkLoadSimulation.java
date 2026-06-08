package javasabr.mqtt.broker.application.loadtest;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.atOnceUsers;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.feed;
import static io.gatling.javaapi.core.CoreDsl.listFeeder;
import static io.gatling.javaapi.core.CoreDsl.randomSwitch;
import static io.gatling.javaapi.core.CoreDsl.repeat;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.mqtt.MqttDsl.mqtt;

import io.gatling.javaapi.core.Choice;
import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.mqtt.MqttProtocolBuilder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.IntStream;

public class MqttTlsNetworkLoadSimulation extends Simulation {

    private static final int CLIENT_COUNT = 5;
    private static final int MAX_ITERATIONS = 50;
    private static final int MESSAGES_PER_ITERATION = 1000;
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

  private final List<Map<String, Object>> clientIds = IntStream
      .rangeClosed(1, CLIENT_COUNT)
      .mapToObj(s -> "LoadTest_" + UUID.randomUUID().getMostSignificantBits())
      .map(clientId -> Map.of("clientId", (Object)clientId))
      .toList();

  Iterator<Map<String, Object>> iter = clientIds.iterator();

    private final MqttProtocolBuilder mqttProtocol = mqtt
        .broker("localhost", 8883)
        .useTls(true)
        .clientId(genClientId -> {
          Object clientId = iter.next().get("clientId");
          genClientId.set("clientId", clientId);
          return clientId.toString();
        });

  private final FeederBuilder<Object> usersFeeder = listFeeder(clientIds)
      .circular();

    private final ScenarioBuilder scn = scenario("MQTT TLS Load Test")
        .exec(mqtt("ConnectTls").connect())
        .feed(usersFeeder)
        .exec(mqtt("SubscribeTls").subscribe("loadtest/#{clientId}/echo").qosAtLeastOnce())
        .repeat(MAX_ITERATIONS).on(
            feed(usersFeeder),
            repeat(MESSAGES_PER_ITERATION).on(
                feed(usersFeeder),
                // Vary QoS
                randomSwitch().on(
                    new Choice.WithWeight(1,
                        feed(usersFeeder).exec(mqtt("PublishQoS0Tls")
                            .publish("loadtest/#{clientId}/echo")
                            .message(StringBody(session -> generatePayload(100)))
                            .qosAtMostOnce())),
                    new Choice.WithWeight(
                        2,
                        feed(usersFeeder).exec(mqtt("PublishQoS1Tls")
                            .publish("loadtest/#{clientId}/echo")
                            .message(StringBody(session -> generatePayload(256)))
                            .qosAtLeastOnce())),
                    new Choice.WithWeight(
                        1,
                        feed(usersFeeder).exec(mqtt("PublishQoS2Tls")
                            .publish("loadtest/#{clientId}/echo")
                            .message(StringBody(session -> generatePayload(512)))
                            .qosExactlyOnce())))
            )
        );

    {
        setUp(
            scn.injectOpen(atOnceUsers(CLIENT_COUNT))
        ).protocols(mqttProtocol);
    }

    private String generatePayload(int size) {
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(size);
        for (int i = 0; i < size; i++) {
            sb.append(ALPHANUMERIC.charAt(rnd.nextInt(ALPHANUMERIC.length())));
        }
        return sb.toString();
    }
}
