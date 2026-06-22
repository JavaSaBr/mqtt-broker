# ADR 0001: Native-image metadata strategy — eliminate the hand-written reachability-metadata.json

- **Status:** Accepted — native compiles with no JSON/Registrar; binary initializes through auth, connection
  service, and TLS/SSL setup. Full runtime (DB + live TLS + MQTT traffic) pending CI.
- **Date:** 2026-06-21
- **Supersedes:** the maintained `native-image/src/main/resources/META-INF/native-image/reachability-metadata.json` (4,631 lines)

## Context

The GraalVM native build of the broker historically relied on a single, hand-maintained
`reachability-metadata.json` (~4,631 lines, ~738 reflection entries) plus zero
`RuntimeHintsRegistrar` classes. Maintaining that file by hand is fragile: every new reflective
third-party dependency or app reflection site requires a hand-edited entry, and a stale or missing
entry surfaces only as a native runtime failure.

Investigation (Gradle cache scan + GraalVM metadata-repository research) showed the file was almost
entirely redundant or coverable by other sources:

- Netty (all modules), Reactor Core/Netty, Log4j2, R2DBC-PostgreSQL, `spring-core`/`spring-web`/
  `spring-boot` **ship their own** `META-INF/native-image/` metadata.
- Jackson, Logback, PostgreSQL-JDBC, SnakeYAML are **covered by the GraalVM Reachability Metadata
  Repository** (enabled via the native plugin; `useLatestConfigWhenVersionIsUntested` also covers
  Flyway 11.19.0 against the repository's 11.14.1 config).
- The 51 `com.sun.crypto.provider.*` / `sun.security.*` entries are JDK security providers, enabled
  by the `--enable-all-security-services` native-image flag.
- The app-owned reflection was `NumberedEnumMap` (external `rlib`, via
  `Class.getEnumConstants()` + `Array.newInstance()` across 12 protocol enums) and `DebugUtils`
  (debug-only Jackson serialization + `StackWalker`).

The goal: ship the native build with **no `RuntimeHintsRegistrar`** (already met) and **no
hand-written `reachability-metadata.json`**.

## Decision

Coverage for native image now comes entirely from Spring AOT, library-shipped metadata, the GraalVM
Reachability Metadata Repository, build flags, and reflection-free app code — never from a
project-maintained JSON file or a `RuntimeHintsRegistrar`.

1. **App reflection removed.**
   - `NumberedEnumMap` (external) replaced by `javasabr.mqtt.model.NumberedEnumLookup` — a
     reflection-free lookup backed by a plain `Object[]`, constructed from `values()`. All 12
     protocol enums migrated; `ofCode` semantics preserved (verified by Spock specs
     `NumberedEnumLookupSpec`, `QoSSpec`, `DisconnectReasonCodeSpec`).
   - `DebugUtils.toJsonString` and `registerIncludedFields` made fail-safe (try/catch with a simple
     fallback; `StackWalker` guarded). A debug `toString()` can no longer crash the app when
     reflection hints are absent, so per-type hints are not required. JVM-mode output is unchanged.

2. **rlib `Array` component types registered via `@RegisterReflectionForBinding`.** The rlib
   `Array`/`ArrayFactory` collections create typed arrays via `java.lang.reflect.Array.newInstance`,
   which requires each component type's array class to be registered. These were the `[]` entries in
   the deleted JSON. They are now declared declaratively on `NativeReflectionHintsConfig` (an
   AOT-processed `@Configuration` imported by `MqttBrokerApplication`) via
   `@RegisterReflectionForBinding({ CredentialsSource[].class, AclRule[].class, Consumer[].class, … })`
   — covering all app/rlib array types the original JSON listed plus the JDK functional-interface
   arrays (`Consumer[]`, `BiConsumer[]`, `UUID[]`) that no library metadata covers. Library-owned
   array types (Log4j2, Spring, reactor, Jackson) are covered by their shipped/repository metadata
   and are not re-declared. (`ExpirableSession`/`NotExpirableSession` were made `public` so their
   array classes can be referenced from the annotation; their constructors stay package-private.)

3. **Native build config (`native-image/build.gradle`).**
   - `graalvmNative { metadataRepository { enabled = true } }` — pulls curated metadata for the
     libraries that do not ship their own (Jackson, Logback, PostgreSQL-JDBC, SnakeYAML, and Flyway
     via latest-config fallback).
   - `--enable-all-security-services` build arg — registers JCE/TLS security providers (replaces the
     51 hand-written `com.sun.crypto.provider` entries). Note: the flag is currently deprecated by
     GraalVM but functional; a future task should migrate to its successor.

4. **`ClassPathResourceResolver` left as-is.** It loads runtime `file:`/`classpath:` URIs via
   `Files.newInputStream`/`ClassLoader.getResourceAsStream`; it is resource loading, not reflection,
   and production paths are `file:` (no resource hint needed). The JSON `resources` entries were
   third-party/META-INF (covered by library metadata + Spring AOT + ServiceLoader support), not app
   classpath resources, so converting to Spring `Resource` would not have deleted any entries.

5. **`reachability-metadata.json` deleted.**

## Consequences

- Positive: no project-maintained native metadata; new reflective third-party dependencies are
  covered automatically by their shipped/repository metadata or Spring AOT; app code is
  reflection-free and idiomatic.
- Trade-off: `DebugUtils` JSON output degrades to a simple fallback in the native image when Jackson
  lacks hints for a type (acceptable for debug-only logging). Richer native debug output can be
  restored later via a curated `@RegisterReflectionForBinding` set — an AOT-processed annotation that
  is neither a `RuntimeHintsRegistrar` nor the JSON file.
- Trade-off: Flyway native support depends on the metadata repository's latest-config fallback for
  11.19.0. If a future Flyway migration relies on untested behavior, pin the module to a covered
  version via `graalvmNative { metadataRepository { moduleToConfigVersion = ['org.flywaydb:flyway-core': '11.14.1'] } }`.

## Verification

- Unit tests: `./gradlew :model:test` (new `NumberedEnumLookupSpec`/`QoSSpec`/`DisconnectReasonCodeSpec`)
  and `./gradlew :base:test` green.
- Spring AOT: `./gradlew :native-image:processAot` succeeds with the refactored beans and the new
  `NativeReflectionHintsConfig`.
- Native compile: `./gradlew :native-image:nativeCompile` succeeds (~2 min) with the JSON removed —
  reflection registrations dropped from 5,941 types (with JSON) to 5,584, confirming the JSON was
  mostly redundant.
- Native runtime smoke (native binary run directly): the broker initializes through Log4j2 → SLF4J →
  Spring context → property binding → `DefaultAuthenticationService` → `DefaultConnectionService`
  (registers all 9 MQTT message handlers, exercising `NumberedEnumLookup`, reason codes, `AclRule[]`
  arrays) → TLS properties → `externalNetworkSslContext` creation (exercises JCE via
  `--enable-all-security-services`). Each earlier failure was either a missing config value or a
  `MissingReflectionRegistrationError` for an rlib-Array component type, all resolved by
  `@RegisterReflectionForBinding` — no unresolvable reflection errors remain.
- Pending (CI): full runtime with a real environment — Postgres for the DB credentials source / Flyway,
  a valid TLS keystore + live handshake, and MQTT client traffic exercising publish/subscribe and
  session/subscription array paths end-to-end.
