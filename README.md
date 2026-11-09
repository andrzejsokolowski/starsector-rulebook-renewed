# Rule Book - Renewed

House rules for one Starsector campaign. Hard rules make the Sector meaner, helpful rules make it
kinder. Every rule has its own level, and you can change any of them at any time.

- **Picking rules**: the Rule Book opens a few seconds after a new game starts (LunaLib setting). Open it
  again from the **Rule Book** entry on the intel screen. A click on a level applies it at once. There is
  no limit on how many rules are active.
- **Mirrors**: most hard rules have a helpful twin that pushes the same number the other way. They sit
  next to each other in the list.
- **Balance meter**: every rule level has points, negative for hard rules and positive for helpful ones.
  The meter shows the total, for example "You're 20 points over budget - Slightly overpowered". It never
  stops a pick.
- **Unlocks**: some rules open up through feats, such as finishing At the Gates. Feats are kept in
  `saves/common`, so they count for every later campaign. The LunaLib setting "Every rule unlocked"
  opens all of them without touching the real unlocks.
- **New Game Plus - Renewed**: the Rule Book writes a career point multiplier that New Game Plus -
  Renewed reads. Each point under budget adds 1 percent, each point over budget takes 1 percent away.

## Rules

Values and points are per level, from level I up.

| Hard rule | Helpful mirror | Values | Hard points | Helpful points |
| --- | --- | --- | --- | --- |
| Hardened Foes: enemy ships take less damage | Brittle Foes: enemy ships take more damage | 5, 10, 15, 20, 25 percent | -6 to -30 | +6 to +30 |
| Deadly Foes: enemy weapons deal more damage | Dull Blades: enemy weapons deal less damage | 5 to 25 percent | -6 to -30 | +6 to +30 |
| Plated Foes: enemy ships have more armor | Rusted Foes: enemy ships have less armor | 10, 20, 30, 40, 50 percent | -5 to -25 | +5 to +25 |
| Hasty Foes: enemy ships and fighters are faster | Sluggish Foes: enemy ships and fighters are slower | 5 to 25 percent | -4 to -20 | +4 to +20 |
| Long Guns: enemy guns reach farther | Short Sights: enemy guns reach shorter | 5 to 25 percent | -5 to -25 | +5 to +25 |
| Swift Pursuers: hostile fleets gain burn | Lumbering Hunters: hostile fleets lose burn | 1, 2, 3 | -5, -10, -15 | +5, +10, +15 |
| Worn Barrels: your weapons deal less damage | Sharp Gunnery: your weapons deal more damage | 5 to 25 percent | -6 to -30 | +6 to +30 |
| Thin Plating: your ships have less armor | Reinforced Plating: your ships have more armor | 10 to 50 percent | -5 to -25 | +5 to +25 |
| Clogged Engines: your ships are slower | Tuned Engines: your ships are faster | 5 to 25 percent | -4 to -20 | +4 to +20 |
| Short Reach: your guns reach shorter | Long Reach: your guns reach farther | 5 to 25 percent | -5 to -25 | +5 to +25 |
| Thin Insulation: overloads last longer | Grounded Conduits: overloads end sooner | 15 to 75 / 10 to 50 percent | -3 to -15 | +3 to +15 |
| Green Crews: less maximum CR | Veteran Crews: more maximum CR | 3, 6, 9, 12, 15 percent | -5 to -25 | +5 to +25 |
| Hungry Fleet: more supply use | Lean Logistics: less supply use | 10 to 50 / 6 to 30 percent | -3 to -15 | +3 to +15 |
| Heavy Drives: your ships lose burn | Hyperspace Drifter: your ships gain burn | 1, 2, 3 | -8, -16, -24 | +8, +16, +24 |
| Clumsy Shipwrights: fewer built-in hull mods | Master Shipwrights: more built-in hull mods | 1, 2 / 1, 2, 3 | -10, -20 | +10, +20, +30 |
| Harsh Worlds: more colony hazard | Gentle Worlds: less colony hazard | 20 to 100 / 10 to 50 percent | -3 to -15 | +4 to +20 |

Helpful rules without a mirror: **Ludd's Song** (gate travel costs no fuel, unlocked by finishing At the
Gates, +15) and **Peerless** (one Peerless point once per campaign, only with Peerless Skills, +10).

Armor rules are a percent of each ship's own armor, so a frigate and a capital change by the same share.
Combat rules apply only in campaign battles, not in missions or the simulator. Rules about your ships
cover your own fleet, except the overload rules, which also cover allied ships in the battle.

## Adding rules

One row per rule in `data/config/rulebook/rules.csv` (other mods can add rows to the same path). The
`effect` column names a class implementing `rulebookrenewed.rules.RuleEffect`; rules with the same `pair`
key are listed together.

Requires LunaLib. Credits in `CREDITS.txt`.
