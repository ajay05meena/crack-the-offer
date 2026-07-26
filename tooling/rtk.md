# RTK (Rust Token Killer)

**In plain English:** if you ask a friend "what's in the fridge?", you don't want them to list all 50 items — you want "milk, eggs, bread, rest is normal." RTK does that for commands like `git status` or `ls`: it runs the command but shrinks the output before the AI has to read it, so it "reads" less and you save tokens (which is like data/credits). It works automatically in the background.

Token-optimized CLI proxy — wraps common dev commands (`git`, `ls`, `grep`, `npm`, `docker`, etc.) and filters/compresses their output before it reaches an LLM's context, cutting token usage on routine operations.

## Install

Binary lives at `~/.local/bin/rtk` (installed via the RTK installer/release binary — not on Homebrew or npm).

```bash
ls -la ~/.local/bin/rtk        # confirm it's there
~/.local/bin/rtk --version     # e.g. rtk 0.43.0
```

`~/.local/bin` is **not** on PATH by default on this machine. Add it to your shell profile:

```bash
# ~/.zshrc
export PATH="$HOME/.local/bin:$PATH"
```

Then reload (`source ~/.zshrc`) and confirm:

```bash
which rtk
rtk --version
```

> Name collision warning: there's an unrelated `reachingforthejack/rtk` ("Rust Type Kit") package. Make sure `which rtk` resolves to `~/.local/bin/rtk`.

## Configure

### Claude Code integration (hook-based, transparent rewriting)

```bash
rtk init --global          # registers the PreToolUse hook in ~/.claude/settings.json
rtk init --global --show   # view current hook/config state
```

This patches `~/.claude/settings.json` with a `PreToolUse` hook on the `Bash` matcher (`rtk hook claude`), which transparently rewrites eligible shell commands (e.g. `git status` → `rtk git status`) before they run — no per-command effort needed.

Useful `init` flags:
- `--agent <claude|cursor|windsurf|cline|...>` — target a different coding agent
- `--dry-run -v` — preview what would be written without touching files
- `--auto-patch` / `--no-patch` — control whether `settings.json` is patched automatically or you apply it manually
- `--uninstall` — remove RTK hooks/artifacts for the selected agent

### Config file

```bash
rtk config              # show current config
rtk config --create     # write a default config file
```

### Telemetry

```bash
rtk telemetry status
rtk telemetry enable | disable | forget   # forget = delete collected data (GDPR/RGPD)
```

## Checking token savings

```bash
rtk gain                 # summary of tokens saved + history
rtk gain --history       # recent command-level savings history
rtk gain --project       # scoped to the current project (cwd)
rtk gain --graph         # ASCII graph of daily savings
rtk gain --daily         # full daily breakdown
rtk gain --weekly        # weekly breakdown
rtk gain --monthly       # monthly breakdown
rtk gain --all           # daily + weekly + monthly combined
rtk gain --quota -t pro  # estimated monthly quota savings for a given plan tier (pro | 5x | 20x)
rtk gain --format json   # or csv, for scripting/export
rtk gain --failures      # commands that fell back to raw (unfiltered) execution
```

Related visibility commands:

```bash
rtk discover          # scan Claude Code history for missed RTK opportunities
rtk session           # RTK adoption across Claude Code sessions
rtk cc-economics      # Claude Code spend (ccusage) vs. RTK savings, side by side
rtk hook-audit         # hook rewrite audit metrics (needs RTK_HOOK_AUDIT=1 env var)
```

## Debugging

```bash
rtk proxy <cmd>   # run the raw command through RTK but still track usage, no filtering
rtk run <cmd>     # run via sh -c, fully raw — no filtering or tracking
rtk verify        # verify hook integrity + run any project-local TOML filter inline tests
```

If a wrapped subcommand ever misbehaves, `rtk proxy` / `rtk run` are the escape hatches to get unfiltered output while you investigate.
