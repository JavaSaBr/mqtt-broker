package javasabr.mqtt.model.subscriber.tree;

import java.util.concurrent.TimeUnit;
import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.subscriber.SingleSubscriber;
import javasabr.mqtt.model.subscription.Subscription;
import javasabr.mqtt.model.subscription.TestMqttUser;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.mqtt.model.topic.tree.SubscriberTreeTest;
import javasabr.rlib.collections.array.ArrayFactory;
import javasabr.rlib.collections.array.MutableArray;
import javasabr.rlib.collections.dictionary.DictionaryFactory;
import javasabr.rlib.collections.dictionary.MutableRefToRefDictionary;
import org.jspecify.annotations.NonNull;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Group;
import org.openjdk.jmh.annotations.GroupThreads;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

@Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 20, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
@Threads(100)
public class SubscriberNodeBenchmark {

  @Param({"100"})
  public int subscribeFrequency;

  private SubscriberNode originalImplementation;
  private OptimizedSubscriberNode optimizedImplementation;

  private TopicName[] topicNames;
  private int[] lastLevels;

  private TopicFilter[] topicFilters;
  private Subscription[] subscriptions;

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

    topicFilters = new TopicFilter[TEST_FILTERS.length];
    subscriptions = new Subscription[TEST_FILTERS.length];

    originalImplementation = new SubscriberNode();
    optimizedImplementation = new OptimizedSubscriberNode();

    for (int i = 0; i < TEST_FILTERS.length; i++) {
      String rawTopic = TEST_FILTERS[i];
      TopicFilter topicFilter = new TopicFilter(rawTopic);
      TestMqttUser owner = new TestMqttUser("init_user_" + i);

      topicFilters[i] = topicFilter;
      subscriptions[i] = SubscriberTreeTest.makeSubscription(rawTopic);

      originalImplementation.subscribe(0, owner, subscriptions[i], topicFilter);
      optimizedImplementation.subscribe(0, owner, subscriptions[i], topicFilter);
    }
  }

  @State(Scope.Thread)
  public static class ThreadState {
    int index = 0;
    public MutableArray<@NonNull SingleSubscriber> container;
    public MutableRefToRefDictionary<MqttUser, SingleSubscriber> mapContainer;
    private TestMqttUser userForSubscription;
    @Setup(Level.Trial)
    public void setupThread() {
      container = ArrayFactory.mutableArray(SingleSubscriber.class);
      mapContainer = DictionaryFactory.mutableRefToRefDictionary();
      userForSubscription = new TestMqttUser("thread_user_" + Thread.currentThread().getId());
    }
  }

  @Group("original")
  @Benchmark
  public void originalImplementationSubscribe(SubscriberNodeBenchmark benchmark, ThreadState state, Blackhole bh) {
    int topicFilterIndex = state.index % topicFilters.length;
    originalImplementation.subscribe(0, state.userForSubscription, subscriptions[topicFilterIndex], topicFilters[topicFilterIndex]);
    bh.consume(topicFilterIndex);
    state.index++;
  }
  @Group("original")
  @Benchmark
  public void originalImplementationUnsubscribe(SubscriberNodeBenchmark benchmark, ThreadState state, Blackhole bh) {
    int topicFilterIndex = state.index % topicFilters.length;
    originalImplementation.unsubscribe(0, state.userForSubscription, topicFilters[topicFilterIndex]);
    bh.consume(topicFilterIndex);
  }
  @Group("original")
  @GroupThreads(5)
  @Benchmark
  public void originalImplementationMatchTo(SubscriberNodeBenchmark benchmark, ThreadState state, Blackhole bh) {
    int topicNameIndex = state.index % topicNames.length;
    originalImplementation.matchesTo(0, topicNames[topicNameIndex], lastLevels[topicNameIndex], state.container);
    bh.consume(state.container);
    state.index++;
  }

  @Group("optimized")
  @Benchmark
  public void optimizedImplementationSubscribe(SubscriberNodeBenchmark benchmark, ThreadState state, Blackhole bh) {
    int topicFilterIndex = state.index % topicFilters.length;
    optimizedImplementation.subscribe(0, state.userForSubscription, subscriptions[topicFilterIndex], topicFilters[topicFilterIndex]);
    bh.consume(topicFilterIndex);
    state.index++;
  }
  @Group("optimized")
  @Benchmark
  public void optimizedImplementationUnsubscribe(SubscriberNodeBenchmark benchmark, ThreadState state, Blackhole bh) {
    int topicFilterIndex = state.index % topicFilters.length;
    optimizedImplementation.unsubscribe(0, state.userForSubscription, topicFilters[topicFilterIndex]);
    bh.consume(topicFilterIndex);
  }
  @Group("optimized")
  @GroupThreads(5)
  @Benchmark
  public void optimizedImplementationMatchTo(SubscriberNodeBenchmark benchmark, ThreadState state, Blackhole bh) {
    int topicNameIndex = state.index % topicNames.length;
    optimizedImplementation.matchesTo(0, topicNames[topicNameIndex], lastLevels[topicNameIndex], state.mapContainer);
    bh.consume(state.container);
    state.index++;
  }
}
