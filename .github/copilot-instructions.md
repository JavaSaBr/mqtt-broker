# MQTT-Broker Copilot Instructions

## Repository Overview

**MQTT-Broker** is an open-source Java-based MQTT broker implementing MQTT v3.1.1 and v5.0 protocol versions. The project consists of ~27,000 lines of code across 283 Java files and 100 Groovy test files, organized as a multi-module Gradle project.

### Key Statistics
- **Project Type**: Multi-module Gradle project
- **Primary Language**: Java 25 with Java Preview Features enabled
- **Test Language**: Groovy (Spock Framework)
- **Build Tool**: Gradle 9.1.0
- **Framework**: Spring Boot 3.5.8 (4.0.0-M1 plugin for application module)
- **Core Library**: RLib (javasabr.rlib) - custom collections and network libraries hosted on GitLab Maven repository

## Project Structure

The repository is organized into the following modules:

### Core Modules
- **base**: Base utilities and Jackson JSON support, requires RLib collections
- **model**: Domain classes for MQTT protocol (QoS, topics, subscriptions, messages, sessions)
- **network**: Network layer implementation for MQTT protocol, message reading/writing
- **core-service**: Service interfaces and implementations (authentication, authorization, publishing, subscriptions)
- **application**: Standalone Spring Boot application with main class `javasabr.mqtt.broker.application.MqttBrokerApplication`
- **embedded**: Library module for embedding MQTT broker in other applications

### ACL (Access Control List) Modules
- **acl-engine**: Order-based priority ACL rules engine (deny by default)
- **acl-groovy-dsl**: Groovy DSL-based ACL configuration parser (HCL-inspired format)
- **acl-service**: Spring Boot auto-configuration for ACL services

### Support Modules
- **test-support**: Common test dependencies (Spock, HiveMQ client, Moquette broker, Spring Test)
- **test-coverage**: Aggregates JaCoCo test coverage reports from all modules

## Build System & Environment

### Required Tools
- **Java 25** (Temurin JDK) - Project uses Java Preview Features with `--enable-preview` flag
- **Gradle 9.1.0** - Auto-downloaded via Gradle Wrapper (./gradlew)
- **Docker** - Mentioned in README but not currently used in build process
- **Network Access** - Required to download dependencies from GitLab Maven repository (https://gitlab.com/api/v4/projects/37512056/packages/maven)




### Important Configuration Files
- `build.gradle` (root) - Main build configuration, version 0.0.1, defines buildSingleArtifact tasks
- `settings.gradle` - Defines 11 project modules with type-safe project accessors enabled
- `gradle/libs.versions.toml` - Version catalog for all dependencies
- `lombok.config` - Lombok configuration with custom logger declaration and fluent accessors
- `buildSrc/src/main/groovy/configure-java.gradle` - Java 25 toolchain, preview features, JUnit configuration
- `buildSrc/src/main/groovy/configure-jacoco.gradle` - JaCoCo test coverage configuration

## Build & Test Commands

### Critical Build Information

**IMPORTANT**: The project requires network access to GitLab Maven repository for RLib dependencies. If you encounter "No address associated with hostname" errors for gitlab.com, this indicates a network connectivity issue that must be resolved before building.

### Compile Commands
```bash
# Compile all main classes
./gradlew classes

# Compile all test classes (RECOMMENDED for CI - mirrors GitHub Actions)
./gradlew testClasses

# Clean build directories
./gradlew clean
```

### Test Commands
```bash
# Run all tests across all modules
./gradlew test

# Run tests for specific module
./gradlew :model:test
./gradlew :network:test
./gradlew :core-service:test

# Run a single Spock spec while iterating on one area
./gradlew :core-service:test --tests 'javasabr.mqtt.service.session.impl.InMemoryMqttSessionServiceTest'

# Tests use JUnit Platform with Groovy/Spock
# Tests run with maxParallelForks=2, forkEvery=100
```

### Build Artifact Commands
```bash
# Build executable JAR (Spring Boot application)
./gradlew :application:bootJar
# Output: application/build/libs/application-0.0.1.jar

# Build all modules and create bootJar in one command
./gradlew buildSingleArtifact

# Build without running tests
./gradlew buildSingleArtifactWithoutTests
```

### Run Commands
```bash
# Run application directly (development mode)
./gradlew :application:bootRun
# Main class: javasabr.mqtt.broker.application.MqttBrokerApplication
# Uses application/src/main/resources/application.properties
# Default credentials file: application/src/main/resources/credentials (format: user=password)

# Run with custom JVM args if needed (tests already include --enable-preview)
# Groovy compilation tasks also include --enable-preview
```

### Coverage & Verification Commands
```bash
# Generate JaCoCo reports for individual modules
./gradlew jacocoTestReport

# Generate aggregated JaCoCo report (after individual reports)
./gradlew test-coverage:testCodeCoverageReport
# Output: test-coverage/build/reports/jacoco/testCodeCoverageReport/

# Run all checks (includes tests and verification)
./gradlew check
```

### Command Execution Notes
- **Always use `./gradlew`** (not `gradle`) to ensure correct Gradle version (9.1.0)
- **Java 25 is required** - Build will fail with older Java versions due to toolchain configuration
- **Preview features are mandatory** - Already configured in build files, don't remove `--enable-preview`
- **Network dependency**: First build requires downloading RLib dependencies from GitLab Maven repository
- **Gradle daemon**: First run will be slower as daemon starts up
- **Timing**: `testClasses` typically takes 30-60 seconds; `test` can take 2-3 minutes depending on parallelism

## GitHub Actions CI Workflow

**File**: `.github/workflows/gradle.yml`

### Build Job Steps (in order)
1. Checkout code (actions/checkout@v4)
2. Set up JDK 25 (Temurin distribution)
3. Setup Gradle (gradle/actions/setup-gradle@v4.0.0)
4. **Compile all modules**: `./gradlew testClasses`
5. **Run tests**: `./gradlew test`
6. Generate JUnit test report (mikepenz/action-junit-report@v5) - requires all tests pass, checks `**/build/test-results/test/TEST-*.xml`
7. Generate JaCoCo reports: `./gradlew jacocoTestReport`
8. Generate aggregated coverage: `./gradlew test-coverage:testCodeCoverageReport`
9. Add coverage to PR (madrapps/jacoco-report@v1.7.2) - min 40% overall, 60% changed files

### Dependency Submission Job
- Runs separately to submit dependency graph to GitHub

### Coverage Requirements
- **Minimum overall coverage**: 40%
- **Minimum coverage for changed files**: 60%
- Coverage report location: `test-coverage/build/reports/jacoco/testCodeCoverageReport/testCodeCoverageReport.xml`

## Code Style & Conventions

### Lombok Configuration
- **Custom logger**: Uses RLib logger API (`javasabr.rlib.logger.api.Logger`)
- **Fluent accessors**: Enabled (getters/setters without get/set prefix)
- **Chaining**: Disabled
- All modules use Lombok with these settings

### Dependencies Exclusions
- **Always excluded**: `slf4j-log4j12`, `spring-boot-starter-logging`
- **Logging framework**: Log4j2 (via spring-boot-starter-log4j2)
- Configuration: `application/src/main/resources/log4j2.xml`

### Stateful Service Conventions
- **Strict managed-state contracts**: For storages and services that manage registered runtime state, prefer explicit failures on invalid operations over silent no-ops when the misuse indicates a programming error
- **Runtime-aligned tests**: When a stricter contract is introduced, update integration tests to create objects through the same storage/service path used in production instead of relaxing the implementation to accept detached objects
- **Concurrency tradeoffs**: If a design uses shared wrappers with atomics after map lookup, treat that as an intentional lifecycle tradeoff; otherwise keep lookup, mutation, and removal under the same lock when stronger consistency matters
- **Lifecycle accounting**: When runtime objects are reference-counted or consumer-counted, model every temporary and long-lived owner explicitly (for example dispatch phase, retained ownership, subscriber delivery) and balance each `+1` with a clearly defined `-1`
- **Stateful naming**: Name methods, fields, and exceptions after the state they actually manage; for example, scheduled-removal APIs should describe scheduling state rather than implying the publish has already been removed from storage
- **Outgoing publish wrappers**: Keep `OutgoingPublish` implementations as transport wrappers around the source `IncomingPublish`; delivery variants may change messageId, QoS, duplicated/retained flags, and subscription IDs, but should reuse the source message content and metadata
- **Tracking before send**: For tracked delivery flows, register message-tracker state, callbacks, and retry handlers before the first network send so retries and response handlers never observe an untracked publish
- **Sender cleanup**: If subscriber delivery contributes to lifecycle accounting, every terminal sender path must release that ownership: success, async failure, invalid user type, missing session, invalid flow state, and abandoned delivery
- **Protocol error handling**: When a tracked publish flow receives an unexpected response type or phase, treat it as a protocol violation: close the client with the appropriate MQTT error and complete lifecycle cleanup instead of only logging the mismatch
- **Retained replacement contracts**: Retain services and retained-tree helpers should return the previous retained publish when replacing or removing retained state so lifecycle ownership can be transferred correctly
- **Retained delete semantics**: Keep the rule that an empty retained payload removes retained state inside the retain service/tree layer instead of scattering that decision across callers
- **CAS return rules**: In CAS-based remove/clear helpers, only the thread that successfully changes the shared state should return the removed object; failed CAS attempts should return `null`, not stale references
- **Debug payload policy**: Prefer compact debug output for payload-bearing objects (IDs, sizes, metadata) rather than logging raw payload bytes

### Testing Conventions
- **Framework**: Spock (Groovy-based BDD framework)
- **Test location**: `src/test/groovy/` directories
- **Base specs**: Use `UnitSpecification` for isolated unit tests and `IntegrationServiceSpecification` for service-level tests that need shared broker fixtures or helper services
- **Integration fixtures**: In integration specs, shared services/storages from `IntegrationServiceSpecification` are intentional and may be used directly to model the real runtime flow
- **Async helpers**: Use `fromAsync(...)` and `waitForAsync(...)` from `BaseSpecification` to unwrap `Mono` and `CompletionStage` results in tests
- **Fixture naming**: Prefer explicit arranged-data names like `testMessageId`, `testTopicName`, and `testUserProperties` over generic local names in new tests
- **Domain defaults**: Prefer MQTT/domain constants and shared empty values such as `MqttInMessage.EMPTY_USER_PROPERTIES` or `MqttProperties.*` over raw literals when building test inputs
- **Real object preparation**: When production behavior depends on an object being registered in shared storage or created through a service, prepare it through the same storage/service path in integration tests instead of constructing a detached test object
- **Lifecycle cleanup**: If a test creates a service with its own lifecycle or background thread (for example `InMemoryMqttSessionService`), close it in a Spock `cleanup:` block
- **Test fixtures**: Available in network and model modules (testFixtures source set)
- **Parallel execution**: Tests run with 2 parallel forks, forking every 100 tests

## Module Dependencies

### Dependency Graph
```
application -> core-service, acl-service, acl-groovy-dsl
embedded -> core-service
core-service -> network, acl-engine
acl-service -> acl-engine, core-service
acl-groovy-dsl -> acl-engine
network -> model
acl-engine -> model
model -> base
base -> (external dependencies only)
```

### External Dependencies (Key Versions)
- RLib: 10.0.alpha9 (collections, network, logger)
- Spring Boot: 3.5.8 (core), 4.0.0-M1 (plugin)
- Spring Framework: 6.2.15
- Project Reactor: 3.7.8
- Jackson (Tools): 3.0.1
- Spock: 2.4-M6-groovy-4.0
- Groovy: 4.0.28
- JUnit Jupiter: 5.13.4
- Lombok: 1.18.38
- HiveMQ MQTT Client: 1.3.10 (testing)

## Known Issues & Workarounds

### Network Connectivity
If build fails with "gitlab.com: No address associated with hostname":
- This indicates the sandboxed environment cannot access GitLab Maven repository
- RLib dependencies (rlib-collections, rlib-network, rlib-logger-*) are hosted on GitLab
- No local workaround available - requires network access to be enabled

### Java Version
- **Java 25 is mandatory** - Do not attempt to build with Java 17 or other versions
- The toolchain configuration in `configure-java.gradle` enforces Java 25
- Preview features are required and configured via `--enable-preview` in all compilation tasks

### TODO/FIXME Comments
The codebase contains TODO comments in several classes related to MQTT protocol implementation. These are feature completeness markers, not bugs. Key areas:
- PublishAck message handling (in/out messages)
- Qos1/Qos2 publish message handlers
- Message factory implementations

## Making Changes

### When Adding New Dependencies
1. Add to `gradle/libs.versions.toml` in appropriate section
2. Reference in module's `build.gradle` using `libs.` prefix
3. Ensure logging exclusions are maintained (no slf4j-log4j12)

### When Adding New Modules
1. Create module directory with `build.gradle`
2. Add to `settings.gradle` with `include(":module-name")`
3. Apply appropriate plugins: `configure-java`, `groovy` (for tests), `java-library`
4. Add to `test-coverage/build.gradle` for coverage aggregation

### When Writing Tests
1. Use Spock framework (Groovy syntax)
2. Place in `src/test/groovy/` maintaining package structure
3. Prefer `UnitSpecification` for low-level unit tests and `IntegrationServiceSpecification` when existing shared services, mocked connections, or broker-oriented fixtures are useful
4. In integration tests, prefer the shared fixtures from `IntegrationServiceSpecification` when that matches how the application wires services together
5. When a contract depends on storage membership or service-managed state, create test objects through the real storage/service helper path instead of directly instantiating detached domain objects
6. If implementation behavior was intentionally tightened to reject misuse, update integration tests to follow the real lifecycle instead of preserving old tolerant assumptions
7. When lifecycle accounting depends on sender completion or retained replacement, add tests for both success and terminal error paths, not just the happy path
8. For async service APIs returning `Mono` or `CompletionStage`, use `fromAsync(...)` for returned values and `waitForAsync(...)` when only completion matters
9. Prefer explicit `test...` fixture variable names and descriptive helper names such as `createAndStorePublish(...)` when the helper both arranges data and performs an action
10. Prefer domain constants and shared empty values over magic literals when building MQTT test messages and publishes
11. For services with background cleanup or delayed expiration, test both the direct API contract and one eventual-cleanup path with a short interval or delay
12. When a storage/service owns secondary resources, assert cleanup of both the primary entry and the related owned resource instead of checking only the main map state
13. Close manually created services or clients in `cleanup:` when they own resources or background work
14. Test fixtures can be used from network and model modules
15. Tests automatically run with preview features enabled

### When Modifying Build Configuration
- Root `build.gradle`: Only for repository-wide settings and custom tasks
- Module `build.gradle`: Module-specific dependencies and configuration
- `buildSrc/`: Shared build logic used across all modules
- Never remove `--enable-preview` from Java compilation or test tasks

### When Writing Summary Files
- Match the requested comparison base exactly (for example `develop` vs current branch, or part-3 vs part-2) and do not mix in changes from earlier parts unless the user asks for full branch scope
- Focus on meaningful behavioral and architectural changes, not raw diff noise or minor refactors
- Group related changes into a few clear themes such as storage lifecycle, publish flow, retained handling, sender behavior, tests, and wiring
- When useful, describe publish or lifecycle changes as short ASCII flow diagrams so ownership transfer and terminal cleanup are easy to follow
- If the user asks for part-only scope, keep only the delta introduced in that part and remove inherited history from earlier summaries

## Validation Steps

Before submitting changes, verify locally:

1. **Compile check**: `./gradlew testClasses` (must succeed)
2. **Run tests**: `./gradlew test` (must succeed)
3. **Generate reports**: `./gradlew jacocoTestReport test-coverage:testCodeCoverageReport`
4. **Check coverage**: Verify changed files have >60% coverage
5. **Run application** (if applicable): `./gradlew :application:bootRun`

These steps mirror the GitHub Actions CI workflow and catch most issues before pushing.

## Repository Files Reference

### Root Directory Files
- `LICENSE` - Apache 2.0 License
- `README.md` - Feature map and basic build instructions
- `lombok.config` - Lombok configuration
- `.gitignore` - Excludes .idea/, .gradle/, build/, out/
- `gradlew`, `gradlew.bat` - Gradle wrapper scripts
- `build.gradle`, `settings.gradle` - Build configuration

### Module Structure
Each module follows:
```
module-name/
├── build.gradle
└── src/
    ├── main/
    │   ├── java/javasabr/mqtt/...
    │   └── resources/
    └── test/
        ├── groovy/javasabr/mqtt/...
        └── resources/
```

### Application Module Specifics
- Main class: `javasabr/mqtt/broker/application/MqttBrokerApplication.java`
- Config: `javasabr/mqtt/broker/application/config/MqttBrokerSpringConfig.java`
- Properties: `application/src/main/resources/application.properties`
  - `authentication.allow.anonymous=false`
  - `credentials.source.file.name=credentials`
- Credentials file: `application/src/main/resources/credentials` (format: `user=password`)
- Logging: `application/src/main/resources/log4j2.xml`

## Trust These Instructions

These instructions were created through comprehensive exploration of the codebase, including:
- All build and configuration files
- README and module-specific documentation  
- GitHub Actions workflow definitions
- Project structure and dependency relationships
- Gradle task definitions and build scripts

**Only search for additional information if these instructions are incomplete, incorrect, or if you're working on a specific domain not covered here.** For most tasks, following these instructions will save significant exploration time.
