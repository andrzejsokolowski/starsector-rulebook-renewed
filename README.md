# Rule Book - Renewed

House rules for one Starsector campaign. Hard rules make the Sector meaner, helpful rules make it
kinder. Every rule has its own level, and you can change any of them at any time.

- **Picking rules**: the Rule Book opens a few seconds after a new game starts (LunaLib setting). Open it
  again from the **Rule Book** entry on the intel screen. Rules are grouped by section: enemy ships,
  your ships, fleet and logistics, the Sector, colonies, and special rules. There is no limit on how many
  rules are active.
- **One row per pair**: a hard rule and its helpful mirror share one row. The hard levels sit left of
  **Off**, the helpful levels right of it, so you pick one side or the other. A click applies at once.
- **Balance meter**: every rule level has points, negative for hard rules and positive for helpful ones.
  The meter shows the total, for example "You're 20 points over budget - Slightly overpowered". It never
  stops a pick.
- **Challenges**: the second tab of the Rule Book lists challenges. A challenge names hard rules to keep
  on and a goal, such as winning 20 battles; some have a time limit. Taking one on raises its rules to the
  required levels. Lowering one of them, going over the challenge's limit on helpful rules, or running
  out of time ends the challenge. Reaching the goal unlocks a reward rule. Only progress made while
  the challenge runs counts.
- **Feats and unlocks**: some rules open up through feats, such as finishing At the Gates, defeating
  Omega or growing a colony to size 6. Feats and finished challenges are kept in `saves/common`, so they
  count for every later campaign. The second tab lists every feat and what it unlocks. The LunaLib
  setting "Every rule unlocked" opens all rules without touching the real unlocks.
- **New Game Plus - Renewed**: the Rule Book writes a career point multiplier that New Game Plus -
  Renewed reads. Each point under budget adds 1 percent, each point over budget takes 1 percent away.

## Rules

Values per level, from level I up. Points are the balance points of level I and of the highest level.

### Enemy ships

| Hard | Helpful | Effect (hard / helpful) | Values per level | Points |
| --- | --- | --- | --- | --- |
| Hardened Foes | Brittle Foes | Enemy ships take less damage to shields, armor and hull / enemy ships take more damage to shields, armor and hull. | 5, 10, 15, 20, 25 percent | -6 to -30 / 6 to 30 |
| Deadly Foes | Dull Blades | Enemy weapons deal more damage / enemy weapons deal less damage. | 5, 10, 15, 20, 25 percent | -6 to -30 / 6 to 30 |
| Plated Foes | Rusted Foes | Enemy ships have more armor / enemy ships have less armor. | 10, 20, 30, 40, 50 percent | -5 to -25 / 5 to 25 |
| Thick-Hulled Foes | Hollow Foes | Enemy ships have more hull / enemy ships have less hull. | 10, 20, 30, 40, 50 percent | -4 to -20 / 4 to 20 |
| Hasty Foes | Sluggish Foes | Enemy ships and fighters are faster in combat / enemy ships and fighters are slower in combat. | 5, 10, 15, 20, 25 percent | -4 to -20 / 4 to 20 |
| Long Guns | Short Sights | Enemy ballistic and energy weapons reach farther / enemy ballistic and energy weapons reach shorter. | 5, 10, 15, 20, 25 percent | -5 to -25 / 5 to 25 |
| Overclocked Foes | Choked Vents | Enemy ships dissipate flux faster / enemy ships dissipate flux slower. | 5, 10, 15, 20, 25 percent | -4 to -20 / 4 to 20 |
| Rapid Loaders | Jammed Feeds | Enemy weapons fire faster / enemy weapons fire slower. | 5, 10, 15, 20, 25 percent | -5 to -25 / 5 to 25 |
| Swarming Wings | Grounded Wings | Enemy fighters are replaced faster / enemy fighters are replaced slower. | 10, 20, 30, 40, 50 percent | -3 to -15 / 3 to 15 |
| Restless Systems | Sluggish Systems | Enemy ship systems cool down and recharge faster / enemy ship systems cool down and recharge slower. | 10, 20, 30, 40, 50 percent | -3 to -15 / 3 to 15 |
| Phase Shadows | Phase Static | Enemy phase ships get more time acceleration while phased, and their cloaks recover faster / enemy phase ships get less time acceleration while phased, and their cloaks recover slower. | 10, 20, 30, 40, 50 percent | -3 to -15 / 3 to 15 |
| Tireless Foes | Fraying Nerves | Enemy ships keep peak performance longer / enemy ships keep peak performance shorter, then lose combat readiness faster. | 10, 20, 30, 40, 50 percent | -3 to -15 / 3 to 15 |
| Seasoned Officers | Raw Officers | Enemy officers you meet have more skills / enemy officers you meet have fewer skills (each keeps at least one). | 1, 2, 3 | -4 to -12 / 4 to 12 |
| Antagonism |  | Enemy ships fight with extra random combat hull mods. | 1, 2, 3 | -4 to -12 |

### Your ships

| Hard | Helpful | Effect (hard / helpful) | Values per level | Points |
| --- | --- | --- | --- | --- |
| Worn Barrels | Sharp Gunnery | Your weapons deal less damage / your weapons deal more damage. | 5, 10, 15, 20, 25 percent | -6 to -30 / 6 to 30 |
| Thin Plating | Reinforced Plating | Your ships have less armor / your ships have more armor. | 10, 20, 30, 40, 50 percent | -5 to -25 / 5 to 25 |
| Brittle Frames | Sturdy Frames | Your ships have less hull / your ships have more hull. | 10, 20, 30, 40, 50 percent | -4 to -20 / 4 to 20 |
| Clogged Engines | Tuned Engines | Your ships are slower in combat / your ships are faster in combat. | 5, 10, 15, 20, 25 percent | -4 to -20 / 4 to 20 |
| Short Reach | Long Reach | Your ballistic and energy weapons reach shorter / your ballistic and energy weapons reach farther. | 5, 10, 15, 20, 25 percent | -5 to -25 / 5 to 25 |
| Leaky Capacitors | Superconductors | Your ships have less flux capacity and dissipation / your ships have more flux capacity and dissipation. | 5, 10, 15, 20, 25 percent | -4 to -20 / 4 to 20 |
| Hungry Guns | Efficient Guns | Your weapons cost more flux to fire / your weapons cost less flux to fire. | 5, 10, 15, 20, 25 percent | -4 to -20 / 4 to 20 |
| Weak Emitters | Hardened Emitters | Your shields take more damage / your shields take less damage. | 5, 10, 15, 20, 25 percent | -4 to -20 / 4 to 20 |
| Thin Insulation | Grounded Conduits | Your ships and your allies' ships stay overloaded longer / overloads on your ships and your allies' ships end sooner. | 15, 30, 45, 60, 75 percent / 10, 20, 30, 40, 50 percent | -3 to -15 / 3 to 15 |
| Green Crews | Veteran Crews | Your ships have less maximum combat readiness / your ships have more maximum combat readiness. | 3, 6, 9, 12, 15 percent | -5 to -25 / 5 to 25 |
| Short Fuses | Long Watches | Your ships keep peak performance shorter / your ships keep peak performance longer. | 10, 20, 30, 40, 50 percent | -3 to -15 / 3 to 15 |
| Slow Hangars | Quick Hangars | Your fighters are replaced slower / your fighters are replaced faster. | 10, 20, 30, 40, 50 percent | -2 to -10 / 2 to 10 |
| Sticky Systems | Primed Systems | Your ship systems cool down and recharge slower / your ship systems cool down and recharge faster. | 10, 20, 30, 40, 50 percent | -3 to -15 / 3 to 15 |
| Bloody Decks | Careful Captains | Your ships lose more crew to hull damage / your ships lose fewer crew to hull damage. | 20, 40, 60, 80, 100 percent / 10, 20, 30, 40, 50 percent | -2 to -10 / 2 to 10 |
| Festering Damage | Patchwork Pride | D-mods on your ships are worse / d-mods on your ships are milder. | 10, 20, 30, 40, 50 percent | -2 to -10 / 2 to 10 |
| Shattering Hulls | Rescue Tugs | Your disabled ships are more likely to be lost for good, and to gain new D-mods / your disabled ships are less likely to be lost for good, and to gain new D-mods. | 15, 30, 45, 60, 75 percent / 10, 20, 30, 40, 50 percent | -2 to -10 / 2 to 10 |
| Jinxed |  | Your weapons and engines malfunction now and then, even at full combat readiness (chance for weapons). | 1, 2, 3, 4, 5 percent | -3 to -15 |
| Blight |  | In combat, your ships lose of their maximum armor every second, down to half. | 0.5, 1, 2 percent | -4 to -12 |
|  | Doomslayer | Enemy ships within 1000 range of the ship you fly lose maneuverability and some flux dissipation. Phase ships lose far more and pay more to stay phased. | 10, 20, 30 percent | 3 to 9 |
|  | Last Stand | When the ship you fly is the last of your own on the field, it gets more flux capacity and dissipation, stays at peak performance, and repairs its hull up to half. | 10, 20, 30 percent | 3 to 9 |

### Fleet and logistics

| Hard | Helpful | Effect (hard / helpful) | Values per level | Points |
| --- | --- | --- | --- | --- |
| Hungry Fleet | Lean Logistics | Your ships use more supplies each month / your ships use fewer supplies each month. | 10, 20, 30, 40, 50 percent / 6, 12, 18, 24, 30 percent | -3 to -15 / 3 to 15 |
| Leaky Tanks | Efficient Drives | Your ships use more fuel / your ships use less fuel. | 10, 20, 30, 40, 50 percent | -2 to -10 / 2 to 10 |
| Slow Repairs | Quick Repairs | Your ships repair and recover combat readiness slower / your ships repair and recover combat readiness faster. | 10, 20, 30, 40, 50 percent | -3 to -15 / 3 to 15 |
| Costly Deployments | Cheap Deployments | Deploying your ships costs more combat readiness and supplies / deploying your ships costs less combat readiness and supplies. | 10, 20, 30, 40, 50 percent | -3 to -15 / 3 to 15 |
| Heavy Drives | Hyperspace Drifter | Your ships lose maximum burn level / your ships gain maximum burn level. | 1, 2, 3 | -8 to -24 / 8 to 24 |
| Clumsy Shipwrights | Master Shipwrights | Your ships can take fewer built-in hull mods / your ships can take more built-in hull mods. | 1, 2 / 1, 2, 3 | -10 to -20 / 10 to 30 |
| Cramped Hulls | Roomy Hulls | Your ships have fewer ordnance points / your ships have more ordnance points. | 5, 10, 15 percent | -6 to -18 / 6 to 18 |
| Short-Staffed | Wide Command | You can command fewer officers / you can command more officers. | 1, 2 / 1, 2, 3 | -8 to -16 / 8 to 24 |
| Garbled Orders | Crisp Orders | You have fewer command points in battle / you have more command points in battle. | 1, 2, 3 | -3 to -9 / 3 to 9 |
| Loud Engines | Quiet Running | Other fleets see yours from farther away / other fleets see yours from closer. | 10, 20, 30, 40, 50 percent | -2 to -10 / 2 to 10 |
| Blind Spots | Keen Sensors | Your sensor range is shorter / your sensor range is longer. | 10, 20, 30, 40, 50 percent | -2 to -10 / 2 to 10 |
| Picked Clean | Rich Wrecks | Salvage from wrecks, derelicts and battles is worth less / salvage from wrecks, derelicts and battles is worth more. | 10, 20, 30, 40, 50 percent | -2 to -10 / 2 to 10 |
| Dead Weight |  | Every mothballed ship lowers the maximum combat readiness of your ships by. | 1, 2, 3 percent | -2 to -6 |
| Famine |  | Each month your crew and marines eat food per 100 heads. Without food, some of them leave. | 2, 4, 6 | -3 to -9 |
|  | Fox's Patience | After your fleet holds still for half a day, other fleets see it from closer and your sensor range is longer. | 25, 50 percent | 3 to 6 |
| Greedy Crews | Loyal Hands | Each month your crew and marines take more than their wages / each month your crew and marines give back of their wages. | 25, 50, 75, 100 percent / 10, 20, 30, 40, 50 percent | -2 to -8 / 2 to 10 |
| Slow Learners | Quick Learners | Battles give less experience / battles give more experience. | 10, 20, 30, 40, 50 percent | -3 to -15 / 3 to 15 |
| Ironman |  | Ships you lose in battle can never be recovered. Enemy ships still can. | - | -15 |

### The Sector

| Hard | Helpful | Effect (hard / helpful) | Values per level | Points |
| --- | --- | --- | --- | --- |
| Swift Pursuers | Lumbering Hunters | Hostile fleets gain maximum burn level / hostile fleets lose maximum burn level. | 1, 2, 3 | -5 to -15 / 5 to 15 |
| Swelling Ranks | Thin Patrols | Fleets sent out by every market that is not yours are larger / fleets sent out by every market that is not yours are smaller. | 10, 20, 30, 40, 50 percent | -5 to -25 / 5 to 25 |
| Pristine Foes | Junker Foes | Fleets from markets that are not yours have higher ship quality, so fewer D-mods / fleets from markets that are not yours have lower ship quality, so more D-mods. | 10, 20, 30, 40, 50 percent | -3 to -15 / 3 to 15 |
| Price Gouging | Free Trade | Tariffs at every market that is not yours are points higher / tariffs at every market that is not yours are points lower, never below zero. | 5, 10, 15, 20, 25 / 5, 10, 15, 20 | -3 to -15 / 4 to 16 |
| Stingy Bounties | Generous Bounties | Bounties posted from now on pay less / bounties posted from now on pay more. | 10, 20, 30, 40, 50 percent | -2 to -10 / 2 to 10 |
| Inflated Prices | Bargain Yards | Ships, weapons and fighter wings cost more to buy / ships, weapons and fighter wings cost less to buy. | 10, 20, 30, 40, 50 percent | -2 to -10 / 2 to 10 |
| Raging Storms | Calm Currents | Hyperspace storm strikes hit harder / hyperspace storm strikes hit softer. | 20, 40, 60, 80, 100 percent / 10, 20, 30, 40, 50 percent | -2 to -10 / 2 to 10 |

### Colonies

| Hard | Helpful | Effect (hard / helpful) | Values per level | Points |
| --- | --- | --- | --- | --- |
| Harsh Worlds | Gentle Worlds | Your colonies have more hazard rating / your colonies have less hazard rating. | 20, 40, 60, 80, 100 percent / 10, 20, 30, 40, 50 percent | -3 to -15 / 4 to 20 |
| Skimming Clerks | Booming Trade | Your colonies earn less income / your colonies earn more income. | 10, 20, 30, 40, 50 percent | -3 to -15 / 3 to 15 |
| Crumbling Works | Tidy Ledgers | Your colonies cost more upkeep / your colonies cost less upkeep. | 10, 20, 30, 40, 50 percent | -2 to -10 / 2 to 10 |
| Restless Colonists | Loyal Colonists | Your colonies have less stability / your colonies have more stability. | 1, 2, 3, 4, 5 | -3 to -15 / 3 to 15 |
| Remote Colonies | Busy Lanes | Your colonies have less accessibility / your colonies have more accessibility. | 5, 10, 15, 20, 25 percent | -2 to -10 / 2 to 10 |

### Special

| Hard | Helpful | Effect (hard / helpful) | Values per level | Points |
| --- | --- | --- | --- | --- |
|  | Ludd's Song | Gate travel costs no fuel. Finish At the Gates to unlock. | - | 15 |
|  | Peerless | Grants Peerless point, once per campaign. Needs Peerless Skills. | 1 | 10 |
|  | Mentor's Legacy | Your officers can reach more levels. Reach level 15 to unlock. | 1, 2 | 10 to 20 |
|  | Charter of Governors | You can employ more administrators. Grow a colony to size 6 to unlock. | 1, 2 | 6 to 12 |
|  | Abyss Ward | Your ships take less energy damage. Defeat Omega, the Threat or the Dwellers to unlock. | 10, 20, 30 percent | 3 to 9 |
|  | Phase Hunter | Enemy phase ships take more damage. Complete the challenge The Ziggurat Gambit to unlock. | 10, 20, 30 percent | 4 to 12 |
|  | Iron Hulls | Your ships and your allies' ships take less armor and hull damage. Complete the challenge Iron Gauntlet to unlock. | 5, 10, 15 percent | 6 to 18 |
|  | Lone Legend | Your flagship deals more damage and takes less. Complete the challenge The Lone Admiral to unlock. | 10, 20, 30 percent | 4 to 12 |
|  | Scarred Veterans | Each D-mod on a ship of yours (up to five) gives it more armor and makes its D-mods milder. Complete the challenge Junkyard Baron to unlock. | 2, 4, 6 percent | 3 to 9 |
|  | Shadow Fleet | Other fleets see yours from closer, and your sensor range is longer. Complete the challenge The Long Night to unlock. | 15, 30, 45 percent | 3 to 9 |
|  | Iron Quartermaster | Your ships use fewer supplies and less fuel. Complete the challenge Lean Years to unlock. | 10, 20, 30 percent | 4 to 12 |
|  | Merchant Prince | Tariffs at every market that is not yours are points lower, and your colonies earn more. Complete the challenge Merchant's Gauntlet to unlock. | 5, 10, 15 percent | 4 to 12 |
|  | Ordo Breaker | Automated enemy ships take more damage. Complete the challenge Ordo Hunt to unlock. | 10, 20, 30 percent | 4 to 12 |
|  | Station Breaker | Enemy stations take more damage. Complete the challenge Fortress Breaker to unlock. | 15, 30, 45 percent | 3 to 9 |
|  | Giant Killer | Enemy capital ships take more damage. Complete the challenge Big Game to unlock. | 10, 20, 30 percent | 4 to 12 |
|  | Blitz Doctrine | Deploying your ships costs less combat readiness and supplies. Complete the challenge Blitz to unlock. | 10, 20, 30 percent | 3 to 9 |
|  | Imperial Charter | Your colonies have more stability. Complete the challenge Colonial Empire to unlock. | 1, 2, 3 | 3 to 9 |
|  | Pilgrim's Blessing | Your ships repair and recover combat readiness faster. Complete the challenge Pilgrim's Ordeal to unlock. | 10, 20, 30 percent | 3 to 9 |
|  | Sol Plating | Your ships have more hull. Complete the challenge Sol Expedition to unlock. Needs Sol Renewed. | 10, 20, 30 percent | 4 to 12 |
|  | Lucky Streak | Salvage from wrecks, derelicts and battles is worth more. Complete the challenge High Roller to unlock. Needs Gambling Den or Coalesced Little Mods. | 10, 20, 30 percent | 2 to 6 |
|  | Omega Insight | Your ship systems cool down and recharge faster. Complete the challenge Omega Apprentice to unlock. Needs Omega Core Everywhere. | 10, 20, 30 percent | 3 to 9 |
|  | Ascendant Worlds | Your colonies earn more income. Complete the challenge Ascendant to unlock. Needs Astral Ascension Renewed. | 10, 20, 30 percent | 3 to 9 |

Armor and hull rules are a percent of each ship's own armor and hull, so a frigate and a capital change
by the same share. Combat rules apply only in campaign battles, not in missions or the simulator. Rules
about your ships cover your own fleet; the overload, Jinxed, Blight and Iron Hulls rules also cover
allied ships in the battle. Bounty rules change bounties posted after you pick them. Enemy officer rules
change each officer once, the first time you meet their fleet.

## Challenges

| Challenge | Keep these rules on | Goal | Helpful rules | Unlocks |
| --- | --- | --- | --- | --- |
| The Ziggurat Gambit | Plated Foes III, Phase Shadows III | Defeat the Ziggurat | none | Phase Hunter |
| Iron Gauntlet | Hardened Foes II, Deadly Foes II, Plated Foes II | Win 20 battles | none | Iron Hulls |
| The Lone Admiral | Short-Staffed II, Green Crews II, Garbled Orders II | Win 10 battles | none | Lone Legend |
| Blitz | Hasty Foes II, Deadly Foes II | Win 10 battles within 90 days | none | Blitz Doctrine |
| Big Game | Seasoned Officers II, Antagonism I | Beat a fleet twice the size of your side, 3 times within 180 days | none | Giant Killer |
| Junkyard Baron | Festering Damage II, Shattering Hulls II, Slow Repairs II | Win 15 battles | up to 10 points | Scarred Veterans |
| The Long Night | Swelling Ranks III, Pristine Foes II, Swift Pursuers II | Hold out for 365 days | none | Shadow Fleet |
| Lean Years | Famine II, Hungry Fleet III, Leaky Tanks II | Hold out for 180 days | none | Iron Quartermaster |
| Merchant's Gauntlet | Price Gouging III, Skimming Clerks II | Hold a million credits | none | Merchant Prince |
| Colonial Empire | Restless Colonists II, Crumbling Works II, Remote Colonies II | Own five colonies | none | Imperial Charter |
| Ordo Hunt | Overclocked Foes II, Tireless Foes II, Hardened Foes II | Defeat a Remnant fleet, 3 times | none | Ordo Breaker |
| Fortress Breaker | Plated Foes II, Thick-Hulled Foes II, Long Guns II | Win a battle against an orbital station, 3 times | none | Station Breaker |
| Pilgrim's Ordeal | Famine I, Heavy Drives I, Slow Repairs II | Walk the Pilgrim's Path | none | Pilgrim's Blessing |
| Sol Expedition (needs Sol Renewed) | Swelling Ranks II, Long Guns II, Heavy Drives I | Finish the Translocator quest in Sol | none | Sol Plating |
| High Roller (needs Gambling Den or Coalesced Little Mods) | Price Gouging II, Skimming Clerks II | Win a spin at the Gambling Den, 10 times | none | Lucky Streak |
| Omega Apprentice (needs Omega Core Everywhere) | Phase Shadows II, Rapid Loaders II | Train an officer under an Omega core | none | Omega Insight |
| Ascendant (needs Astral Ascension Renewed) | Restless Colonists II, Crumbling Works II, Harsh Worlds II | Ascend a colony past size 10 | none | Ascendant Worlds |

## Feats

Vanilla: Finish The Kallichore Archive, Return the ISS Hamatsu, Find Coureuse, Finish Project Ziggurat, Defeat the Ziggurat, Finish At the Gates, Find the hyperspace oddity, Finish The Usurpers, Finish Princess of Persea, Win the duel in Princess of Persea, Finish Knight Errant, Walk the Pilgrim's Path, Finish False Idols, Bring back The Stray Flock, Finish Passage To Volturn, Recover the planetkiller, Earn Kanta's protection, Reach level 15, Found a colony, Grow a colony to size 6, Own five colonies, Hold a million credits, Win a battle, Defeat a Remnant fleet, Defeat Omega, the Threat or the Dwellers, Win a battle against an orbital station, Beat a fleet twice the size of your side.

- **Sol Renewed**: Reach Sol, Finish the Translocator quest in Sol, Defeat the Shroud in Sol, Deter the Sol crisis.
- **Gambling Den**: Win a spin at the Gambling Den, Pull a Gambling Den slot lever.
- **Starsector Cloning**: Build a Cloning Megafactory.
- **Reverse Engineered Private Arsenal**: Reverse-engineer 10 weapons, Reverse-engineer a ship.
- **StarterPack**: Start a campaign with a Starter Pack.
- **Missions - Renewed**: Sign a Missions - Renewed contract.
- **Astral Ascension Renewed**: Ascend a colony past size 10, Build an Ecumenopolis.
- **Omega Core Everywhere**: Train an officer under an Omega core.
- **Chicken's Tech Shop**: Meet Chicken, Max out Chicken's Tech Market.
- **Extra Submarkets**: Visit the Great Mall.
- **New Game Plus - Renewed**: Refit a ship at the New Game Plus Ledger.

Battle feats are counted per campaign, so a challenge can ask for them more than once. Feats from other
mods show only while that mod is enabled.

## Adding rules and challenges

One row per rule in `data/config/rulebook/rules.csv` and one row per challenge in
`data/config/rulebook/challenges.csv` (other mods can add rows to the same paths). The rule `effect`
column names a class implementing `rulebookrenewed.rules.RuleEffect`; rules with the same `pair` key
share one row in the picker, under their `section`. A rule with `challenge:<id>` in its `unlock` column
is the reward of that challenge. `requiredMod` may name several mods with `|` between them; one of them
must be enabled.

New Game Plus - Renewed reads one sector memory key: `$rbr_ngpMultiplier`, the career point
multiplier.

Requires LunaLib. Credits in `CREDITS.txt`.
