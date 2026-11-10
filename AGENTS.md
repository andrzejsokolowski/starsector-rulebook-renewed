# Rule Book - Renewed

These instructions apply to this repository, including when it is opened without the parent mod workspace.
The workspace `AGENTS.md` one folder up still governs releases, packaging, Git identity and communication.

## Notion

Tasks, history and test lists: the **Rule Book - Renewed board** in Andrzej's Notion, under **Starsector mods**
(`collection://f990341a-1237-40df-94d1-81f721238d4c`). Ticket ids start with RBR. Read the board with
`notion-query-data-sources` mode `view` on `view://41320ca7-5d20-81cc-887f-000cc352a0bd`. Design decisions:
the page "Rule Book - Renewed: design" (`41320ca75d2081c0ba48d8f8e3dc4b50`) in Claude Information. Update the
board during the work, not only at release time.

## Other mods' work

- Unofficial New Game Plus (Originem; the installed copy is the 0.98 fork 2.3.2). Its licence: "Code are free
  to use and distribute, but it's better to notice me. DO NOT copy entirely. All rights reserved." On
  2026-11-10 Andrzej also got a yes from the fork's author to continue its work and told this project to use
  its code and art. Taken so far: the picker's layout and flow, `ui/UIRect`, `ui/UIPanelManager`,
  `ui/BackgroundUI`, the tile and background art under `graphics/rulebook` (rule icons recoloured per rule
  by a script) and the four tile sounds. Every copied or adapted file says so in its header and
  `CREDITS.txt` lists it. Never copy the mod wholesale; its music was not taken. In 0.9.0 its golden rules and
  the rules with a mechanic of their own were adapted as effect classes in `rules/ungp` (our hooks, not its
  framework); `Tools` there replaces LazyLib. Its plain stat rules were not, since ours cover them.
- Unofficial Rule Book (DemonX3) is MIT. Its rule effects may be adapted; keep its notice in `CREDITS.txt` and
  name the source rule there and in the effect class comment.

## Text

Player-facing text is short and plain, worded like the game: "Enemy armor increased by {value}%." No flavour
lines, no explaining paragraphs (Andrzej, 2026-11-10).

## Balance rules (Andrzej, 2026-11-10)

- At most three levels. The standard curve is 1x, 1.5x, 2.5x of the level 1 number (20/30/50, 10/15/25),
  with points on the same curve. Count rules (burn level, officers, stability) go 1/2/3.
- A minor effect is not worth three small levels: make it one strong single-level rule (Calm Currents: no
  storm damage; Greedy Crews: wages doubled), or two levels where the top one has a price (Loyal Hands).
- A rule locked behind another mod (its challenge or achievement) must be about that mod: Super Sol for Sol
  Renewed, Lucky Streak for Gambling Den. Never lock a general rule behind an unrelated mod.
- Golden rules (`golden` column) are the strong, strange ones; only one can be on.
- A text per level is written with `||` in the description (Clumsy Shipwrights level 3).

## Build

`./gradlew releaseZip` compiles against the game, LunaLib, Achievements - Renewed (its jar in the folder next
door) and Peerless Skills (`gradle.properties` names the
game folder), runs the checks and writes `RuleBookRenewed.zip` in this folder.

## Layout

- `data/config/rulebook/rules.csv` - one row per rule: type (`positive` or `negative`; a rule just for fun is
  a positive rule worth 0 points), levels, points and values per level (`|` separated), required mod, unlock
  (`feat:<id>`, `challenge:<id>` or `achievement:<id>` for another mod's achievement), effect class, icon, description with `{value}`, order, pair, section.
  Loaded merged over all mods by `RuleCatalog` at startup; rows whose required mod is off are dropped before
  their effect class is loaded. A negative rule and its positive mirror share a `pair`; only one can be on.
  Points: negative rules are below zero and give the player points, positive rules cost them; the picker
  shows the cost (`-points`). With the LunaLib setting "Point budget" on, a set that costs more than it
  gives cannot be confirmed.
- `data/config/rulebook/challenges.csv` - one row per challenge: required rules (`rule_id:level|...`), goal
  (`feat:<id>[:<count>]`, `wins:<n>` or `days:<n>`), helpfulLimit (points of positive rules allowed, 0 = none,
  empty = no cap), deadlineDays. Nothing is taken on: `Challenges.check` (daily, after battles, after every
  rule change) counts progress whenever a challenge's conditions hold. Counted goals pause when they do
  not; "hold out" goals start over; a one-time deed must happen while they hold; a state goal that was
  already true (five colonies, a million credits) counts after 90 days. Completion is the achievement
  `rbr_challenge_<id>`, which opens rules whose unlock is `challenge:<id>`.
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
  More hooks: `advanceCombat` (every unpaused frame of a campaign battle: Doomslayer, Last Stand),
  `processCombatInput` (the same frame's input events), `advanceCampaign` (every unpaused campaign frame),
  `onPlayerBattleFinished` (after every battle the player is in) and `prepareEnemyFleet` (once per enemy
  fleet when an encounter opens: enemy officer skills, marked per officer).
  A hard rule and its mirror are never on together: `RuleManager.setLevel` switches the other side off.
- `RuleCombatPlugin` (registered in `data/config/settings.json`) stands down outside campaign battles.
- `RuleState` - chosen levels per save (sector persistent data). `Unlocks` - a thin layer over
  Achievements - Renewed (a required mod, `achievementsrenewed.api.Achievements`): the feat `x` is the
  achievement `rbr_x`, a finished challenge is `rbr_challenge_<id>`. Their rows (name, hint, how-to, reward
  line, chain, tab) are in `data/config/achievements_renewed/achievements.csv`. `tab` is `vanilla` for
  base-game achievements, otherwise the name of the mod the achievement is about (never rank Oddisz's mods
  above others; Andrzej, 2026-11-10); feat rows name the script
  `achievements/FeatAchievement`, which reports the feat's count, and that mod polls it, keeps what is done
  across saves and shows the notification. `Feats` - every feat with a count: story flags (0 or 1), colonies,
  credits, and battle counters from `Counters` (per save, filled by `RuleBattleListener` when the player's
  side wins). Feats from Oddisz's other mods (`modFlag`/`modCount`) read only state those mods leave in the
  save, never their classes, and exist only while one of their mod ids is enabled. Checked daily and after
  battles; the build reads the feat ids from `Feats.java` and `Counters.java` to check the CSV files.
  Challenge column `deadlineDays` is a time limit. Challenges only unlock Rule Book rules; they never pay
  New Game Plus career points (Andrzej, 2026-11-10). Rule Book writes one thing for New Game Plus - Renewed:
  `$rbr_ngpMultiplier`. New Game Plus - Renewed (0.7.0 on) pays its own career points, through its own
  listener, for every achievement except our challenges, which it recognises by the id prefix
  `rbr_challenge_` and the category `Challenges`. So never rename an `rbr_*` achievement or change its
  condition, and keep that prefix and category for challenges. Our `rbr_ngp_ledger` reads its hull mod id
  `ngp_ledger_refit`.
- `Balance` - meter text and the New Game Plus - Renewed multiplier, written to sector memory
  `$rbr_ngpMultiplier` (1 + net points under budget / 100, at least 0.1).
- `ui/RulePickerPanel` - UNGP's picker layout with native UI: positive grid over negative grid (or the
  challenge list) on the left; on the right the hovered rule (only the effect that applies, large), the
  picked rules, challenges whose rules are met, points, problems, Confirm / Cancel. Picks go to a working
  copy and are applied on Confirm. `RuleTile` is one tile (area checkbox, icon, level pips, cost): left
  click picks then raises the level, right click lowers it (mouse polled in `advance`); the mirror of a
  picked rule is disabled ("Conflict with X"); a locked rule shows as `[ ? ]` with "Unlocked by". Each tab
  is a page panel built once and moved off screen when hidden (never remove a scrolling TooltipMakerAPI
  with removeComponent). `ChallengeRow` is one challenge with its rule icons and "Enable required rules",
  which replaces the working rules with exactly the required ones; a later pick that breaks that
  challenge, or one with progress, shows a warning box once. `Presets` keeps four saved rule sets in saves/common
  (`rulebook_renewed_presets.json`); the buttons sit next to the tabs. `RuleBookDialog` hosts the picker;
  `RuleBookIntel` is the permanent intel entry with the open button.
- `compat/peerless` - the only package that may name Peerless Skills (`checkPeerlessIsolated`).
