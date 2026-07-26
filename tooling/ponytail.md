# Ponytail

**In plain English:** this one changes *how the AI writes code*, not how much text it reads. It makes the AI act like an experienced "lazy" senior programmer who always asks: "Do I really need to write this, or does a simpler built-in way already exist?" For example, instead of installing a whole calendar library just to pick a date, it reminds the AI that browsers already have a built-in date picker — so it uses that one line instead of 50. Result: simpler, shorter code.

Claude Code plugin that makes the agent default to minimal-code solutions — a YAGNI ladder (skip → reuse → stdlib → native feature → existing dependency → one-liner → only then write more) applied before any implementation.

Full docs: https://github.com/DietrichGebert/ponytail

## Install

```
/plugin marketplace add DietrichGebert/ponytail
/plugin install ponytail@ponytail
```

Must be two separate prompts — sending both in one message doesn't register the install. After installing, run:

```
/reload-plugins
```

## Configure

Default intensity, in `~/.config/ponytail/config.json`:

```json
{ "defaultMode": "full" }
```

Or per-shell via env var: `PONYTAIL_DEFAULT_MODE=lite|full|ultra|off`. Default is `full`.

To scope the ruleset away from specific subagents (e.g. keep it off read-only search agents), set `PONYTAIL_SUBAGENT_MATCHER` to a regex tested against the subagent's `agent_type` (unanchored, case-insensitive).

## Checking it's working

```
/plugin list     # should show ponytail@ponytail installed
/ponytail        # reports current mode (no arg = report only)
```

## Commands

```
/ponytail [lite|full|ultra|off]   # set intensity, or turn off
/ponytail-review                   # review current diff for over-engineering
/ponytail-audit                    # audit whole repo, not just the diff
/ponytail-debt                     # harvest deferred `ponytail:` shortcuts into a ledger
/ponytail-gain                     # measured impact scoreboard (LOC/cost/time)
/ponytail-help                     # quick reference
```

## Gotchas

- Requires `node` on PATH for the lifecycle hooks (same PATH prerequisite as [`tooling/rtk.md`](rtk.md) / [`tooling/headroom.md`](headroom.md)). Without it, the `/ponytail*` skills still work but always-on activation silently stays quiet instead of erroring.
- Uninstalling with `/plugin remove ponytail` leaves state behind outside the plugin folder (mode flag, `~/.config/ponytail/config.json`, a `statusLine` entry). Run `node scripts/uninstall.js` from a checkout first if you want that cleaned up too — it must run *before* the remove, since the script is itself a plugin file.
