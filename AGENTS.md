# Rule Book - Renewed

These instructions apply to this repository, including when it is opened without the parent mod workspace.
The workspace `AGENTS.md` one folder up still governs releases, packaging, Git identity and communication.

## Notion

Tasks, history and test lists: the **Rule Book - Renewed board** in Andrzej's Notion, under **Starsector mods**
(`collection://f990341a-1237-40df-94d1-81f721238d4c`). Ticket ids start with RBR. Read the board with
`notion-query-data-sources` mode `view` on `view://41320ca7-5d20-81cc-887f-000cc352a0bd`. Design decisions:
the page "Rule Book - Renewed: design" (`41320ca75d2081c0ba48d8f8e3dc4b50`) in Claude Information. Update the
board during the work, not only at release time.

## Clean room

- Unofficial New Game Plus (Originem) is all rights reserved. Never open its `src`, jar, CSV or string files.
  Game ideas are fine; names, text and code are written from scratch.
- Unofficial Rule Book (DemonX3) is MIT. Its rule effects may be adapted; keep its notice in `CREDITS.txt` and
  name the source rule there and in the effect class comment.

## Build

`./gradlew releaseZip` compiles against the game, LunaLib and Peerless Skills (`gradle.properties` names the
game folder), runs the checks and writes `RuleBookRenewed.zip` in this folder.

## Layout

- `data/config/rulebook/rules.csv` - one row per rule: levels, points and values per level (`|` separated),
  required mod, unlock (`feat:<id>` or `challenge:<id>`), effect class, icon, description with `{value}`, flavor,
  unlock hint, order, pair, section (the picker heading). Loaded merged over all mods by `RuleCatalog` at
  startup; rows whose required mod is off are dropped before their effect class is loaded. Sections sort by
  their lowest order, so keep each section's orders together.
- `data/config/rulebook/challenges.csv` - one row per challenge: required rules (`rule_id:level|...`), goal
  (`feat:<id>[:<count>]`, `wins:<n>` or `days:<n>`), helpful limit (points, 0 = none allowed, empty = no cap).
  `Challenges` loads it after the rules. A run is kept in the save with its start time and the goal feat's
  count at the start, so only progress during the run counts. Lowering a required rule or going over the
  helpful limit ends the run (`Challenges.validate`, called from `RuleManager.setLevel`). Completion is the
  feat `challenge_<id>` in `Unlocks`, which opens rules whose unlock is `challenge:<id>`.
- `src/rulebookrenewed/rules` - `RuleEffect` hooks: campaign apply/unapply (every 0.25 days and on change),
  `onSwitchedOn`, `applyToShip` (combat, first frame each ship is on the field), `applyToMember` (fleet buff),
  `applyToEnemyMember` (enemy ships before they are built). A hard rule and its helpful mirror share one
  effect class; `RuleEffect.enemySign` / `playerSign` turn the rule's type into the direction, values stay
  positive in the CSV. The `pair` column groups mirrors in the picker.
  `MemberStatRule` keeps a `RuleMemberBuff` on every player ship (buffs survive stat rebuilds and expire a day
  after the last refresh). `CombatStatRule` changes one side's live ship stats (damage, speed, range,
  overload). `EnemyMemberStatRule` is for stats fixed at ship creation (armor): `BattlePrep` buffs enemy
  fleet members when the encounter dialog opens (`RuleBattleListener`) and every frame for ships still in the
  enemy reserve (`RuleCombatPlugin`), and removes the buffs when the battle ends. The armor grid's per-cell
  maximum is set only in its constructor (checked in the 0.98a jar), so armor cannot be raised after a
  ship exists. `HostileFleetBurn` changes hostile fleets in the player's system and hyperspace.
  Other bases: `FleetStatRule` (the player fleet's campaign stats), `CharacterStatRule` (the player's
  character stats: officers, command points, ordnance points), `MarketRule` (the player's colonies or every
  other market), `EnemyKindDamage` (one kind of enemy ship takes more damage). `Scale.by` does the percent
  push: a rise is a percent bonus, a cut is a multiplier with a 0.05 floor.
- `RuleCombatPlugin` (registered in `data/config/settings.json`) stands down outside campaign battles.
- `RuleState` - chosen levels per save (sector persistent data). `Unlocks` - feats in saves/common
  (`rulebook_renewed_unlocks.json`). `Feats` - every feat with a count: story flags (0 or 1), colonies,
  credits, and battle counters from `Counters` (per save, filled by `RuleBattleListener` when the player's
  side wins). Checked daily and after battles; the build reads the feat ids from `Feats.java` and
  `Counters.java` to check the CSV files.
- `Balance` - meter text and the New Game Plus - Renewed multiplier, written to sector memory
  `$rbr_ngpMultiplier` (1 + net points under budget / 100, at least 0.1).
- `ui/RulePickerPanel` - two tabs (Rules; Challenges and feats), each one scrolling list rebuilt on a tab
  switch (labels in a panel inside a scrolling TooltipMakerAPI, so the game
  clips and scrolls them) with self-drawn, self-hit-tested buttons (see Gambling Den) that follow the
  content panel's real screen position each frame. `RuleBookDialog` hosts it;
  `RuleBookIntel` is the permanent intel entry with the open button.
- `compat/peerless` - the only package that may name Peerless Skills (`checkPeerlessIsolated`).
