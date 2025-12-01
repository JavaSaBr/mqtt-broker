package javasabr.mqtt.model.subscriber.tree;

import java.util.concurrent.TimeUnit;
import javasabr.mqtt.model.subscriber.SingleSubscriber;
import javasabr.mqtt.model.subscription.Subscription;
import javasabr.mqtt.model.subscription.TestMqttUser;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.mqtt.model.topic.tree.SubscriberTreeTest;
import javasabr.rlib.collections.array.ArrayFactory;
import javasabr.rlib.collections.array.MutableArray;
import org.jspecify.annotations.NonNull;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

@Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 20, time = 3, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Threads(10)
public class SubscriberNodeBenchmark {

  private SubscriberNode originalImplementation;
  private OptimizedSubscriberNode optimizedImplementation;

  private TopicName[] topicNames;
  private int[] lastLevels;

  private static final String[] TEST_TOPIC_NAMES = {
      "home/kitchen/sensor/temp",
      "home/livingroom/light/state",
      "system/events/alert/priority/low",
      "command/reboot/device/101",
      "$SYS/broker/clients/total",
      "data",
      "metrics/cpu/usage",
      "user/id_12345/settings",
      "long/path/with/many/segments/is/unlikely/but/possible",
      "a/b/c/d/e/f/g",
      "short"
  };

  private static final String[] TEST_FILTERS = {
      "home/#",
      "home/kitchen/sensor/temp",
      "home/+/sensor/temp",
      "home/+/+/temp",
      "system/events/+/priority/#",
      "$SYS/#",
      "$SYS/broker/clients/+",
      "#",
      "+",
      "+/+/+/+",
      "a/b/c/d/e/f/g/#",
      "+/kitchen/sensor/temp",
      "data",
      "data/#",
      "command/+",
      "system/events/#",
      "user/+/settings"
  };

  @Setup
  public void setup() {
    topicNames = new TopicName[TEST_TOPIC_NAMES.length];
    lastLevels = new int[TEST_TOPIC_NAMES.length];
    for (int i = 0; i < TEST_TOPIC_NAMES.length; i++) {
      topicNames[i] = new TopicName(TEST_TOPIC_NAMES[i]);
      lastLevels[i] = topicNames[i].levelsCount() - 1;
    }

    originalImplementation = new SubscriberNode();
    optimizedImplementation = new OptimizedSubscriberNode();
    for (String rawTopic : TEST_FILTERS) {
      TopicFilter topicFilter = new TopicFilter(rawTopic);
      Subscription subscription = SubscriberTreeTest.makeSubscription(rawTopic);
      TestMqttUser owner = new TestMqttUser("id");
      originalImplementation.subscribe(0, owner, subscription, topicFilter);
      optimizedImplementation.subscribe(0, owner, subscription, topicFilter);
    }
  }

  @State(Scope.Thread)
  public static class ThreadState {
    int index = 0;
    public MutableArray<@NonNull SingleSubscriber> container;

    @Setup(Level.Trial)
    public void setupThread() {
      container = ArrayFactory.mutableArray(SingleSubscriber.class);
    }
  }

  @Benchmark
  public void originalImplementation(ThreadState state, Blackhole bh) {
    int i = state.index % topicNames.length;
    originalImplementation.matchesTo(0, topicNames[i], lastLevels[i], state.container);
    bh.consume(state.container);
    state.index++;
  }

  @Benchmark
  public void optimizedImplementation(ThreadState state, Blackhole bh) {
    int i = state.index % topicNames.length;
    optimizedImplementation.matchesTo(0, topicNames[i], lastLevels[i], state.container);
    bh.consume(state.container);
    state.index++;
  }
}
