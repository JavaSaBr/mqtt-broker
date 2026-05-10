# create-tests

Create or extend tests for this MQTT broker using the repository's established testing patterns.

## Use this skill when

- adding tests for new behavior or bug fixes
- extending coverage for storage, processor, sender, or session lifecycle changes
- deciding whether a new spec should be a unit or integration test
- updating tests after a stricter managed-state contract was introduced

## Repository-specific testing rules

- Use **Spock** and place tests under `src/test/groovy/` with matching package structure.
- Use `UnitSpecification` for isolated logic and `IntegrationServiceSpecification` for broker/service flows with shared fixtures.
- In integration tests, shared services and storages from `IntegrationServiceSpecification` are intentional and may be used directly.
- When behavior depends on managed storage or service state, create objects through the real storage/service path instead of detached constructors.
- If implementation was intentionally tightened to reject misuse, update tests to follow the real runtime lifecycle instead of preserving tolerant assumptions.
- For async APIs returning `Mono` or `CompletionStage`, use `fromAsync(...)` or `waitForAsync(...)`.
- For delayed cleanup or background expiration, test both the direct API contract and one eventual-cleanup path with a short delay.
- When a storage or service owns secondary resources, assert cleanup of both the primary entry and the owned resource.
- Close lifecycle-owned services or clients in `cleanup:`.

## Test style

- Prefer explicit fixture names like `testMessageId`, `testTopicName`, and `testUserProperties`.
- Prefer MQTT/domain constants and shared empty values such as `MqttInMessage.EMPTY_USER_PROPERTIES` and `MqttProperties.*`.
- Prefer descriptive helpers such as `createAndStorePublish(...)` when a helper both arranges data and performs an action.
- Cover both happy-path and terminal/error cleanup behavior when lifecycle accounting or sender completion matters.

## Workflow

1. Read the production code and nearby tests before adding anything.
2. Reuse existing fixtures, helpers, and base specs instead of creating parallel test infrastructure.
3. Add the smallest set of tests that fully covers the new contract, including negative and cleanup paths where relevant.
4. Run the most targeted Gradle test command first, then broaden to the relevant module suite if needed.

## Useful commands

```bash
./gradlew :core-service:test --tests 'fully.qualified.SpecName'
./gradlew :module-name:test --tests 'fully.qualified.SpecName'
./gradlew :core-service:test
```

## Common patterns in this repository

- Storage tests often assert both in-memory map state and owned `PublishData` cleanup.
- Integration publish-flow tests often prepare publishes through `defaultIncomingPublishStorage` and publish data through `defaultPublishDataStorage`.
- Stateful lifecycle tests may use `UnitSpecification.waitUntil(...)` for short eventual-cleanup assertions.
