# Rule Book - Renewed

House rules for one Starsector campaign. Hard rules make the Sector meaner, helpful rules make it
kinder. Every rule has levels, and you can change them at any time.

- **Picking rules**: the Rule Book opens a few seconds after a new game starts (LunaLib setting). Open it
  again from the **Rule Book** entry on the intel screen. A click on a level applies it at once. There is
  no limit on how many rules are active.
- **Balance meter**: every rule level has points, negative for hard rules and positive for helpful ones.
  The meter shows the total, for example "You're 20 points over budget - Slightly overpowered". It never
  stops a pick.
- **Unlocks**: some rules open up through feats, such as finishing At the Gates. Feats are kept in
  `saves/common`, so they count for every later campaign. The LunaLib setting "Every rule unlocked"
  opens all of them without touching the real unlocks.
- **New Game Plus - Renewed**: the Rule Book writes a career point multiplier that New Game Plus -
  Renewed reads. Each point under budget adds 1 percent, each point over budget takes 1 percent away.

## Rules in 0.1.0

| Rule | Type | Levels | Effect | Points |
| --- | --- | --- | --- | --- |
| Hardened Foes | hard | 3 | Enemy ships take 10, 20, 30 percent less damage | -10, -20, -30 |
| Deadly Foes | hard | 3 | Enemy weapons deal 10, 20, 30 percent more damage | -10, -20, -30 |
| Thin Insulation | hard | 3 | Your and allied ships stay overloaded 20, 40, 60 percent longer | -5, -10, -15 |
| Hungry Fleet | hard | 3 | Your ships use 15, 30, 50 percent more supplies | -5, -10, -20 |
| Harsh Worlds | hard | 3 | Your colonies get 25, 50, 100 percent more hazard rating | -5, -10, -15 |
| Veteran Crews | helpful | 3 | Your ships get 5, 10, 15 percent more maximum combat readiness | +10, +20, +30 |
| Lean Logistics | helpful | 3 | Your ships use 10, 20, 30 percent fewer supplies | +5, +10, +20 |
| Hyperspace Drifter | helpful | 2 | Your ships get 1, 2 more maximum burn | +10, +25 |
| Ludd's Song | helpful | 1 | Gate travel costs no fuel (unlocked by finishing At the Gates) | +15 |
| Peerless | helpful | 1 | 1 Peerless point once per campaign (only with Peerless Skills) | +10 |

Combat rules apply only in campaign battles, not in missions or the simulator.

## Adding rules

One row per rule in `data/config/rulebook/rules.csv` (other mods can add rows to the same path). The
`effect` column names a class implementing `rulebookrenewed.rules.RuleEffect`.

Requires LunaLib. Credits in `CREDITS.txt`.
