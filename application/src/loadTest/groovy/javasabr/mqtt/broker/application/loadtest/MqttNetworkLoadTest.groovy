package javasabr.mqtt.broker.application.loadtest

import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PayloadFormatIndicator
import groovy.util.logging.Slf4j
import javasabr.mqtt.broker.application.IntegrationSpecification
import javasabr.mqtt.broker.application.MqttClientFactory

import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.LongAccumulator

@Slf4j
class MqttNetworkLoadTest extends IntegrationSpecification {

    static final int MAX_ITERATIONS = 5
    static final int MAX_SEND_DELAY = 10_000
    static final int CLIENT_COUNT = 100
    static final int MESSAGES_PER_ITERATION = 1000

    static class StatisticsCollector {

        final LongAccumulator publishedMessagesPerSecond = new LongAccumulator(Long::sum, 0L)
        final LongAccumulator receivedMessagesPerSecond = new LongAccumulator(Long::sum, 0L)
        final LongAccumulator totalPublished = new LongAccumulator(Long::sum, 0L)
        final LongAccumulator totalReceived = new LongAccumulator(Long::sum, 0L)
    }

    static class TestClient implements AutoCloseable {

        private static final ALPHANUMERIC = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        private static final RANDOM = new Random()

        final String clientId
        final Mqtt5AsyncClient client
        final StatisticsCollector statistics

        TestClient(String clientId, InetSocketAddress address, StatisticsCollector statistics) {
            this.clientId = clientId
            this.statistics = statistics
            this.client = MqttClientFactory.buildMqtt5Client(clientId, address)
        }

        void connectAndSubscribe() {
            client.connect().join()
            client.subscribeWith()
                .topicFilter("loadtest/$clientId/echo")
                .qos(MqttQos.AT_MOST_ONCE)
                .callback({ publish ->
                    statistics.receivedMessagesPerSecond.accumulate(1)
                    statistics.totalReceived.accumulate(1)
                })
                .send().join()
        }

        void sendMessages(List<String> targetClientIds) {
            def executor = Executors.newSingleThreadScheduledExecutor()
            def random = ThreadLocalRandom.current()
            def tasks = []

            try {
                for (int iteration in 0..<MAX_ITERATIONS) {
                    for (int m = 0; m < MESSAGES_PER_ITERATION; m++) {
                        int delay = random.nextInt(MAX_SEND_DELAY)
                        def targetId = targetClientIds[random.nextInt(targetClientIds.size())]
                        def task = executor.schedule({
                            def payload = generatePayload(10, 256)
                            client.publishWith()
                                .topic("loadtest/$targetId/echo")
                                .qos(MqttQos.AT_MOST_ONCE)
                                .payload(payload)
                                .payloadFormatIndicator(Mqtt5PayloadFormatIndicator.UTF_8)
                                .send()
                                .thenAccept({
                                    statistics.publishedMessagesPerSecond.accumulate(1)
                                    statistics.totalPublished.accumulate(1)
                                })
                        }, delay, TimeUnit.MILLISECONDS)
                        tasks << task
                    }

                    for (task in tasks) {
                        task.get()
                    }
                    tasks.clear()
                }
            } finally {
                executor.shutdown()
            }
        }

        static byte[] generatePayload(int minLength, int maxLength) {
            def length = minLength + RANDOM.nextInt(maxLength - minLength + 1)
            def chars = new char[length]
            for (int i = 0; i < length; i++) {
                chars[i] = ALPHANUMERIC[RANDOM.nextInt(ALPHANUMERIC.size())]
            }
            return new String(chars).getBytes(StandardCharsets.UTF_8)
        }

        @Override
        void close() {
            try {
                client.disconnect().join()
            } catch (Exception ignored) {}
        }
    }

    def "test broker handles concurrent publishers and subscribers"() {
        given:
            def statistics = new StatisticsCollector()
            def scheduledExecutor = Executors.newSingleThreadScheduledExecutor()
            def clients = []

            initStatisticsReporter(scheduledExecutor, statistics)

            for (int c = 0; c < CLIENT_COUNT; c++) {
                def clientId = MqttClientFactory.generateClientId("LoadTest")
                def testClient = new TestClient(clientId, externalPlainNetworkAddress, statistics)
                testClient.connectAndSubscribe()
                clients << testClient
            }

            def clientIds = clients.collect { it.clientId }
            int expectedTotal = CLIENT_COUNT * MESSAGES_PER_ITERATION * MAX_ITERATIONS

        when:
            def threads = clients.collect { client ->
                Thread.start(client.clientId) {
                    def targetIds = clientIds - client.clientId
                    client.sendMessages(targetIds)
                }
            }
            threads.each { it.join() }

            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(300)
            while (statistics.totalReceived.get() < expectedTotal && System.nanoTime() < deadline) {
                Thread.sleep(1000)
            }

        then:
            statistics.totalReceived.get() >= expectedTotal

        cleanup:
            clients.each { it.close() }
            scheduledExecutor.shutdown()
    }

    private static void initStatisticsReporter(
            ScheduledExecutorService executor,
            StatisticsCollector statistics) {
        executor.scheduleAtFixedRate({
            long published = statistics.publishedMessagesPerSecond.getThenReset()
            long received = statistics.receivedMessagesPerSecond.getThenReset()
            println("Published: ${published} msg/sec, Received: ${received} msg/sec, " +
                    "Total published: ${statistics.totalPublished.get()}, " +
                    "Total received: ${statistics.totalReceived.get()}")
        }, 1, 1, TimeUnit.SECONDS)
    }
}
