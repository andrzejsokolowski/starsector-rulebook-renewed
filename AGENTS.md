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

- `data/config/rulebook/rules.csv` - one row per rule: type (`hard`, `helpful`, or `neutral` for fluff worth
  0 points), levels, points and values per level (`|` separated),
  required mod, unlock (`feat:<id>` or `challenge:<id>`), effect class, icon, description with `{value}`, flavor,
  unlock hint, order, pair, section, pairName (the shared tile name of a mirrored pair). Loaded merged over all mods by `RuleCatalog` at
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
  other market), `EnemyKindDamage` (one kind of enemy ship takes more damage), `SettingRule` (scales
  settings.json values the game reads live: bounties, battle XP, ship prices; originals kept and put back).
  `Scale.by` does the percent push: a rise is a percent bonus, a cut is a multiplier with a 0.05 floor.
  More hooks: `advanceCombat` (every unpaused frame of a campaign battle: Doomslayer, Last Stand) and
  `prepareEnemyFleet` (once per enemy fleet when an encounter opens: enemy officer skills, marked per officer).
  A hard rule and its mirror are never on together: `RuleManager.setLevel` switches the other side off.
- `RuleCombatPlugin` (registered in `data/config/settings.json`) stands down outside campaign battles.
- `RuleState` - chosen levels per save (sector persistent data). `Unlocks` - feats in saves/common
  (`rulebook_renewed_unlocks.json`). `Feats` - every feat with a count: story flags (0 or 1), colonies,
  credits, and battle counters from `Counters` (per save, filled by `RuleBattleListener` when the player's
  side wins). Feats from Oddisz's other mods (`modFlag`/`modCount`) read only state those mods leave in the
  save, never their classes, and exist only while one of their mod ids is enabled. Checked daily and after
  battles; the build reads the feat ids from `Feats.java` and `Counters.java` to check the CSV files.
  Challenge column `deadlineDays` is a time limit. Challenges only unlock Rule Book rules; they never pay
  New Game Plus career points (Andrzej, 2026-11-10). The only link to New Game Plus - Renewed is
  `$rbr_ngpMultiplier`.
- `Balance` - meter text and the New Game Plus - Renewed multiplier, written to sector memory
  `$rbr_ngpMultiplier` (1 + net points under budget / 100, at least 0.1).
- `ui/RulePickerPanel` - tile grids on the left (tabs Rules, Challenges, Feats; Rules has a Neutral grid
  of pair tiles and fluff over a Helpful-only and a Hard-only grid), a side panel on the right (balance, summary, hovered tile's details, challenge button).
  Size follows the screen. Each tab is a container panel built once; a tab switch moves the others off
  screen (never remove a scrolling TooltipMakerAPI with removeComponent: the panel holds its external
  scroller, so the labels stay). Tile labels sit in a content panel inside a scrolling TooltipMakerAPI;
  tiles and buttons are self drawn and hit tested (see Gambling Den) and follow the content panel's real
  screen position each frame. Left click is one step easier, right click one step harder. `RuleBookDialog` hosts it;
  `RuleBookIntel` is the permanent intel entry with the open button.
- `compat/peerless` - the only package that may name Peerless Skills (`checkPeerlessIsolated`).
