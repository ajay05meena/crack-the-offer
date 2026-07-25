---
name: commit
description: Reviews and creates a git commit for this repo — refuses to commit directly on `main` (prompts to branch instead), inspects the actual staged/unstaged diff before staging anything, and updates the root README's `## Index` table when the diff adds or changes a problem/exercise/design doc that isn't reflected there yet. Use when the user asks to commit, "make a commit", "commit my changes", "commit this branch's work".
---

# Commit

Create a commit for this repo the way it should always be done here: never straight onto `main`, never blind about what's staged, and never leaving the README's index stale.

## Workflow

1. **Branch guard — check before anything else.**
   ```
   git branch --show-current
   ```
   If it's `main` (or `master`): STOP. Do not stage or commit anything yet. Tell the user they're on main and ask whether to create a new branch off it (propose a kebab-case name based on the diff, e.g. `add-<exercise-name>`) via `git checkout -b <name>`. Only commit after they've moved off main. Do not commit to main even if the user goes along with it passively — you need an explicit, unambiguous override in their message (e.g. "commit to main anyway"); a plain "yes" to "should I branch first?" is not that override.

2. **See what's actually changing.**
   ```
   git status --short
   git diff
   git diff --staged
   ```
   Read the real diff, don't infer from the file list alone. Flag anything that looks wrong before staging: build output (`build/`, `.gradle/`, `.idea/`), editor/OS junk, credentials or `.env` files, or files unrelated to the stated change. Ask before including anything surprising — don't silently stage it and don't silently drop it either.

3. **Check the README index.** Compare the changed paths against the `## Index` tables in the root `README.md`:
   - A new file under `dsa/app/src/main/java/com/ajay/dsa/<topic>/<Problem>.java` not listed in the DSA table → add a row (new topic → new row group).
   - A new top-level dir under `coding-practice/<exercise-name>/` not listed → add a row with a one-line description (infer from the code or its own README; ask the user if it's not obvious).
   - A new dir under `system-design/<problem-name>/` not listed → add a row linking its doc.
   - A `design-patterns/**/*.md` file that was an empty stub and now has real content → flip its status from `stub` to a one-line summary; a genuinely new pattern file → add a row.
   - Anything new under `ai-skills/` → add a row and drop the "empty" placeholder line.

   If the index needs a change, edit `README.md` and fold it into the same commit — an index update is never a separate follow-up commit. If nothing's missing, say so in one line and move on.

4. **Stage precisely and commit.** Stage the specific files involved (never `-A` or `.`), draft a concise commit message focused on *why* the change was made, and create the commit following this session's standard git commit protocol (HEREDOC message, `Co-Authored-By` trailer, a new commit rather than an amend). Show the user the message as part of committing, per usual practice.

## Notes

Don't push. This skill only prepares and creates the local commit — pushing is a separate, explicit ask.
