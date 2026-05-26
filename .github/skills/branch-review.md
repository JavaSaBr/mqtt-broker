# branch-review

Review a branch, commit range, or uncommitted diff for substantive issues in this MQTT broker.

## Use this skill when

- reviewing a feature branch against `develop` or another feature branch
- reviewing uncommitted changes before commit
- checking whether a recent fix really resolved the previously reported issue
- looking for lifecycle, protocol-flow, storage, or cleanup bugs without nitpicking style

## Review priorities in this repository

- Focus on **real correctness issues**, not formatting, naming preferences, or unfinished TODO-marked work.
- Prefer bugs, protocol mismatches, state leaks, ownership leaks, invalid cleanup, wrong contracts, and broken tests.
- Treat staged or intentionally incomplete work as non-issues if the user explicitly says the missing part is planned.

## Stateful and lifecycle review checklist

- Verify every lifecycle-counted owner has a matching release path.
- Check sender and processor terminal paths: success, async failure, invalid user/session, invalid flow state, and abandoned delivery.
- Check tracked publish flows register tracker/callback state before first send.
- Check protocol-flow mismatches close the client and perform cleanup instead of only logging.
- Check retained replacement/removal returns the previous retained publish when ownership transfer depends on it.
- Check CAS/remove helpers return removed objects only on successful mutation.
- Check scheduled-cleanup APIs, exceptions, and fields are named after the actual state they manage.
- Check background cleanup removes both the primary entry and any owned secondary resource.
- Check `close()` implementations call `thread.interrupt()` in addition to setting the closed flag, so background threads don't sleep a full interval after shutdown.
- Check paginated loops process the last page before breaking — a common mistake is fetching a page, detecting end-of-data, and breaking before scanning the fetched items.

## Test-aware review rules

- Compare implementation changes with the relevant Spock specs, not just the production code.
- If a stricter contract is introduced, verify integration tests create objects through the real storage/service path.
- For delayed cleanup or retry flows, look for one direct-contract test and one eventual-cleanup or terminal-path test.
- Prefer reviewing existing tests for gaps before suggesting new ones.

## Workflow

1. Start from the exact diff against the requested base branch or the current worktree.
2. Read changed files and the most relevant neighboring tests together.
3. Trace the changed flow end to end across storage, processor, sender, retain, and session code when needed.
4. Report only issues that are likely to matter in runtime behavior or test reliability.
5. If re-reviewing after a fix, explicitly check only the previously reported paths plus tightly coupled cleanup.

## Useful commands

```bash
git --no-pager diff base-branch...HEAD -- path/to/file
git --no-pager diff --stat base-branch...HEAD
./gradlew :core-service:test --tests 'fully.qualified.SpecName'
```

## Common review patterns in this repository

- Review publish lifecycle changes across `IncomingPublishStorage`, processors, senders, retained-publish handling, and session trackers together.
- Pay special attention to QoS 1/2 flow state, message tracker updates, `incomingProcessingPublishes` / `outgoingProcessingPublishes`, and late-response handling.
- Treat shared fixtures in `IntegrationServiceSpecification` as intentional when evaluating integration tests.
