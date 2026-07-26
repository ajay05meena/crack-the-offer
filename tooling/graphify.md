# Graphify

**In plain English:** this turns your entire project (code, notes, configs) into a **map** — like a mind-map showing "this file uses that function, which connects to that database table." Instead of the AI reading every single file to understand your project, it can look at this map and quickly find what connects to what. It's like having an index/table-of-contents for your whole codebase instead of reading the whole book every time.

Turns a codebase (code, docs, SQL schemas, configs, PDFs) into a queryable knowledge graph — local, deterministic AST parsing, no vector store. Registers as a `/graphify` skill for Claude Code (and other assistants).

Full docs: https://github.com/Graphify-Labs/graphify

## Install

> Official package is `graphifyy` (double-y) on PyPI — other `graphify*` packages aren't affiliated. The CLI command itself is `graphify`.

```bash
uv tool install graphifyy      # recommended — isolated env, avoids the pip PATH/module gotchas below
```

Then register the skill:

```bash
graphify install                # global (~/.claude/skills/graphify), patches ~/.claude/CLAUDE.md
graphify install --project      # project-scoped instead (./.claude/skills/graphify/SKILL.md)
```

Verify:

```bash
which graphify
graphify --version
```

## Configure

Optional extras (install only what you need), e.g.:

```bash
uv tool install "graphifyy[pdf]"      # PDF extraction
uv tool install "graphifyy[mcp]"      # MCP stdio server
uv tool install "graphifyy[neo4j]"    # push to Neo4j
uv tool install "graphifyy[all]"      # everything
```

## Checking it's working / usage

```
/graphify .        # build/query the graph for the current repo, inside Claude Code
```

## Gotchas

- Same PATH prerequisite as [`tooling/rtk.md`](rtk.md) / [`tooling/headroom.md`](headroom.md) — `~/.local/bin` must be on `PATH` (already set up in `~/.zshrc` on this machine).
- Avoid plain `pip install` on Mac/Windows if possible — the skill resolves its Python runtime relative to where the package lives, and a `pip`-installed copy can throw `ModuleNotFoundError: No module named 'graphify'`. `uv tool install` / `pipx install` isolate it and sidestep this.
- `uvx graphify …` doesn't work — `uvx` needs the *package* name, not the command: `uvx --from graphifyy graphify install`.
