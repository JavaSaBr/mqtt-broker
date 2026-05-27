# generate-branch-summary

Generate a concise, well-structured summary of a feature branch for this MQTT broker repository.

## Use this skill when

- a user asks for a branch summary
- a user asks to document what changed in a branch or part
- a user asks to generate a summary after a review or implementation is complete

## Output file

- Always write the summary to `summary.md` at the **repository root**
- Do not include a top-level H1 title row — start directly with `## Overview`

## Diff scope

- Compare the current branch against `develop` (or the explicitly requested base)
- If the branch is one part of a multi-part series and earlier parts are already merged to `develop`, compare only against `develop` — do not include inherited history from earlier parts
- Use `git --no-pager diff base-branch...HEAD` to get the exact delta

## Structure

The file must contain these sections in order:

### `## Overview`

One short paragraph describing:
- what problem or feature this branch addresses
- the main mechanisms introduced or changed
- no bullet points — prose only

### `## Flow diagrams`

Include this section only when ASCII flow diagrams add meaningful clarity to ownership transfer, lifecycle steps, protocol sequences, or cleanup paths.
Omit the section entirely if there is nothing worth diagramming.

### `## Main changes`

Group related changes into a small number of named subsections (typically 4–8).
Each subsection should:
- be named after the component or behavioral concern it addresses (e.g. `### Scheduled removal lifecycle`, `### TOCTOU race hardening`)
- describe the behavioral or architectural change, not raw diff noise
- include a table for test coverage changes when multiple test files are affected

## Content rules

- Focus on meaningful behavioral and architectural changes
- Do not describe minor refactors, renames, or formatting changes
- Do not repeat flow diagrams inline inside `## Main changes` — reference the diagram section instead
- When describing publish or lifecycle changes, summarise ownership transfer and terminal cleanup clearly
- If the branch is one part of a series, include only the delta introduced in this part
