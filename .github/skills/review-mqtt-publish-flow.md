# review-mqtt-publish-flow

Deep-review MQTT publish delivery flow and lifecycle behavior in this broker, with emphasis on missed cleanup, wrong phase handling, and ownership bugs.

## Use this skill when

- reviewing changes to incoming or outgoing publish processing
- investigating suspected publish leaks, stuck message ids, or duplicate delivery bugs
- checking QoS 0/1/2 flow correctness across processor, sender, storage, and session code
- analyzing retained publish ownership or delayed-cleanup behavior
- validating whether a fix really closes all terminal lifecycle paths

## Primary review target

Trace the full publish lifecycle, not a single class in isolation:

- publish registration and storage
- dispatch ownership and subscriber counting
- retain ownership transfer
- sender delivery and terminal cleanup
- session tracker and processing-publish registration/removal
- protocol response and retry handling
- eventual cleanup or timeout behavior

## Repository-specific publish-flow checklist

- Verify `IncomingPublish` is created through storage when the runtime contract depends on managed state.
- Verify `OutgoingPublish` stays a transport wrapper around `source()` and preserves source-backed content/metadata.
- Verify tracker state, callbacks, and retry handlers are registered before first send.
- Verify every lifecycle owner has a matching release path: dispatch phase, retained ownership, subscriber delivery, retry abandonment, and timeout cleanup.
- Verify sender cleanup covers success, async failure, invalid user type, missing session, invalid flow state, and protocol violation.
- Verify unexpected response types or phases close the client and perform cleanup instead of only logging.
- Verify late or missing QoS 1/2 acknowledgements do not leave message ids busy or processing callbacks registered forever.
- Verify retained replacement/removal returns the previous retained publish when ownership transfer depends on it.
- Verify delayed-cleanup paths remove both the publish entry and any owned `PublishData`.
- Verify scheduled-removal APIs and exceptions describe scheduling state, not a different storage state.
- Verify CAS-based clear/remove helpers return removed objects only on successful mutation.

## QoS-specific review prompts

### QoS 0

- Does cleanup happen when async send completes, including failure?
- Is source ownership released exactly once?

### QoS 1

- Is the message tracked before send?
- Is source ownership released on `PUBACK`, error response, missing tracker state, unexpected response, and protocol close?

### QoS 2

- Is `PUBREC` / `PUBREL` / `PUBCOMP` phase state updated consistently?
- Does timeout or scheduled cleanup also clear session-side state when needed?
- Do late `PUBREL` and unknown-message-id paths produce the correct MQTT result and cleanup?

## Files commonly involved

- `IncomingPublishStorage` / `InMemoryIncomingPublishStorage`
- incoming publish processors, especially `AbstractIncomingPublishProcessor`, `Qos1IncomingPublishProcessor`, `Qos2IncomingPublishProcessor`
- subscriber senders, especially `AbstractSubscriberPublishSender`, `TrackableSubscriberPublishSender`, `Qos1SubscriberPublishSender`, `Qos2SubscriberPublishSender`
- retain services and retained tree/node implementations
- session trackers and processing-publish implementations
- related Spock specs under `core-service/src/test/groovy/.../publish/...`

## Test-aware rules

- Compare changed flow code with the closest integration specs and storage tests together.
- Look for both happy-path and terminal-path assertions.
- For delayed cleanup, expect one direct contract test and one eventual-cleanup test with a short delay.
- If a stricter managed-state contract exists, verify tests create publishes through the real storage/service path.

## Workflow

1. Start from the exact changed flow or bug report.
2. Read processor, sender, storage, retain, and session-tracker code together.
3. Draw the ownership transitions mentally: who increments, who decrements, and on which terminal path.
4. Check tests for the same path and note missing lifecycle assertions.
5. Report only meaningful bugs, leaks, protocol mismatches, or missing cleanup.

## Useful commands

```bash
git --no-pager diff base-branch...HEAD -- core-service/src/main/java/javasabr/mqtt/service/publish
git --no-pager diff base-branch...HEAD -- model/src/main/java/javasabr/mqtt/model/publish model/src/main/java/javasabr/mqtt/model/topic
./gradlew :core-service:test --tests 'javasabr.mqtt.service.publish.processor.Qos2IncomingPublishProcessorTest'
./gradlew :core-service:test --tests 'javasabr.mqtt.service.publish.sender.Qos2SubscriberPublishSenderTest'
```

## Common mistakes this skill should look for

- message tracker updated too late or never cleared
- processing callback left registered after terminal flow
- source publish consumer count not decremented on some terminal branch
- retained ownership transferred without decrementing the replaced publish
- stale object returned from failed CAS clear/remove
- timeout cleanup removing storage state but leaving session state behind
- tests covering success only while missing failure, late-response, or timeout paths
