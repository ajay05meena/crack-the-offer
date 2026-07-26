# Headroom AI

**In plain English:** similar idea to RTK, but for everything, not just commands. It's like a smart filter sitting between the AI and the world — if some tool hands the AI a giant wall of text (like a huge log file), Headroom squeezes it down to the important bits before it reaches the AI. Think of it like a summarizer that shrinks big text into short text without losing the key meaning.

Context-compression proxy for LLM coding agents. Wraps a CLI (Claude Code, Codex, etc.) or sits as a drop-in proxy, compressing tool outputs/logs/RAG chunks before they hit the model's context (~20% savings for coding agents, 60-95% for JSON).

Full docs: https://github.com/headroomlabs-ai/headroom

## Install

The npm package (`headroom-ai`) is **library-only** — no CLI. For the `headroom` command, use `pip`/`uv` instead:

```bash
uv tool install --python 3.13 "headroom-ai[all]"
```

Installs the `headroom` binary to `~/.local/bin` (same PATH prerequisite as [`tooling/rtk.md`](rtk.md) — make sure `~/.local/bin` and `$(brew --prefix)/bin` are on `PATH` in `~/.zshrc`).

Verify:

```bash
headroom --version
headroom doctor
```

## Configure / run

```bash
headroom proxy --port 8787     # just the proxy — point any Anthropic/OpenAI-compatible client at it
headroom wrap claude           # starts the proxy, sets ANTHROPIC_BASE_URL, launches a fresh Claude Code session through it
```

`headroom wrap claude` launches a **new** interactive `claude` process — it can't retroactively route a session that's already running. Run it in a fresh terminal tab/window, not inside an existing Claude Code session.

Other wrap targets: `codex`, `aider`, `cursor`, `cline`, `continue`, `goose`, `opencode`, and more — see `headroom wrap --help`.

Useful `wrap claude` flags:
- `--memory` — persistent cross-session memory
- `--resume <id>` — resume a session through the proxy
- `--1m` — preserve the 1M context window (otherwise capped at 200k behind a custom `ANTHROPIC_BASE_URL`)
- `--no-proxy` — reuse an already-running proxy instead of starting a new one

## Checking it's working / savings

```bash
headroom doctor      # proxy reachability, routing status, shell env, savings recorded
```

Inside a wrapped session, `doctor` should show `proxy ✓ pass`, `claude ✓ pass`, `shell env ✓ pass`. Traffic/compression stats are also available at the proxy's own endpoints (`/stats`, `/stats-history`, `/metrics`) while it's running.

## Gotchas

- PATH: same root cause as RTK — if `headroom`/`claude` aren't found in a brand-new terminal, `~/.zshrc` isn't being sourced as an interactive shell (check terminal app's "login shell" setting) or hasn't been reloaded since install.
- `headroom wrap claude` can't rewrap an already-running Claude Code session — always run it fresh.
