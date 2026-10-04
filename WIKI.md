---
title: Too Many Bows
description: "Too Many Bows Wiki"
icon: bow
---

# Too Many Bows
![bamf](https://media.forgecdn.net/attachments/1017/632/shulker.png)
This page documents bows currently present in the **Too Many Bows** mod, including abilities, special firing requirements, and how to repair them.

> If a bow does not list a required resource, it uses **standard arrows** by default.

---

## 🏹 Bow Abilities & Requirements

| Bow                    | Ability / Effect                                                               | Special Ammo / Resource        |
| ---------------------- |--------------------------------------------------------------------------------| ------------------------------ |
| **Arcane Bow**         | Fires 3 arrows in a spread pattern with arcane damage                          | Standard arrows                |
| **Dark Bow**           | Enhanced damage against undead                                                 | Standard arrows                |
| **Ancient Sage Bow**   | Pierces **33% of armor defenses**                                              | Standard arrows                |
| **Verdant Viper**      | Arrows leave a poisonous cloud on impact                                       | Standard arrows                |
| **Wind Bow**           | Knockback + magic dmg. Grants Speed II + Slow Fall      1.21  only             | Standard arrows                |
| **Demon’s Grasp**      | Dormant weapon; its tooltip describes no remaining power                       | Standard arrows                |
| **Aether’s Call**      | Enemies hit are lifted with Levitation, grants Slow Falling  1.21 only         | Standard arrows                |
| **Spectral Bow**       | _(Not fully documented yet)_                                                   | Standard arrows                |
| **Cyroheart Bow**      | Fires piercing icicle javelins; summons ice on block impact                      | Standard arrows                |
| **Pyre’s Embrace**     | Shoots flaming arrows that cause explosions                                    | Standard arrows                |
| **Necro Flame Bow**    | Applies **Cursed Flame**: blocks regen, can’t be extinguished, damage per tick | Standard arrows                |
| **Tidal Bow**          | Shoots normally underwater + 2.5× underwater damage                            | Standard arrows                |
| **Hunter's Bow**       | Strong vs passive mobs (cows, pigs, sheep, chickens, rabbits)                  | Standard arrows                |
| **Sentinel’s Wrath**   | Strong vs raid mobs (pillagers, vindicators, evokers, etc.)                    | Standard arrows                |
| **Frostbite**          | Shoots frost arrows that slow and freeze enemies                               | Standard arrows                |
| **Burnt Relic**        | Infinite arrows + enhanced damage                                              | Standard arrows                |
| **Ironclad Bow**       | Magnetic Pulse: pulls mobs and player toward arrow impact                      | Standard arrows                |
| **Arc of the Heavens** | Arrows strike lightning on direct hit                                          | Standard arrows                |
| **Solar Flare**        | Shoots flaming solar arrows when fully charged                                 | Standard arrows                |
| **Dragon’s Breath**    | Spawns Dragon’s Breath on impact                                               | Standard arrows                |
| **Shulker’s Blast**    | Fires homing shulker projectiles                                               | Standard arrows                |
| **Scatter Shot**       | Releases a burst of arrows in all directions                                   | Standard arrows                |
| **Emerald Sage Bow**   | Grants bonus XP on hit                                                         | Standard arrows                |
| **Vitality Weaver**    | Leeching arrows drain enemies and heal you                                     | Standard arrows                |
| **Astral Bound**       | Ricocheting arrows (enhanced by Ricochet enchant)                              | Standard arrows                |
| **Spectral Whisper**   | Arrows phase through blocks and damage enemies                                 | Standard arrows                |
| **Aurora’s Grace**     | Celestial arrows powered by rift energy                                        | ✅ Requires **Rift Shards**    |
| **Verdant Vigor**      | Healing aura + regeneration effects when held                                  | Standard arrows                |
| **Twin Shadows**       | Fires twin arrows: one of light and one of darkness                            | Standard arrows                |
| **Crimson Nexus**      | Lifedrain arrows powered by your own health                                    | ✅ Consumes **HP per shot**    |
| **Radiance**           | Radiant arrows that harm undead, heal allies, blind enemies                    | ✅ Consumes **XP per shot**    |
| **Dusk Reaper**        | Marks enemies, creates spectral zones, causes explosions                       | ✅ Consumes **Soul Fragments** |
| **Ethereal Hunter**    | Fires arrows using your hunger instead of arrows                               | ✅ Consumes **Hunger**         |
| **Webstring Volley**   | Shoots 5 arrows in a wide spread + slowness                                    | Standard arrows                |
| **Torchbearer Bow**    | Places torches on impact + emits light when held                               | Standard arrows                |


| **Gravewire Bow** | Curses marked targets and lashes nearby foes with necrotic chains | Standard arrows |
| **Vaultpiercer Bow** | Opens arcane vaults above the target, releasing follow-up arrows | Standard arrows |
| **Eventide** | Full-draw ability hits call five astral lances; links marked targets and strengthens final judgment through repeat hits | Standard arrows |
| **Worldeater** | Guided ability arrows trigger a dragon dive and lingering dragonfire; kills build Dominance for an empowered shot | Standard arrows |
| **Godsplitter** | Steady perfect shots pierce enemies and bypass armor; headshots deal bonus damage | Standard arrows |
| **Blunted Edge** | Trial bow that becomes Godsplitter after three combat challenges | Standard arrows |
| **Dormant Celestial Bow** | Ritual bow that awakens into Eventide | One arrow for the awakening ritual |

---

##  Eventide — Celestial Awakening

### Collect the components

Each component has a **25% chance per matching chest** by default. These chances are controlled by `fragmentChance` in `forbidden_three.json`.

| Component | Chest source |
| --- | --- |
| **Celestial Limb** | End City treasure |
| **Celestial String** | Ancient City |
| **Celestial Grip** | Stronghold library |

Craft one of each component together in any arrangement to obtain the **Dormant Celestial Bow**.

### Awaken the bow

1. Bring the dormant bow and an arrow to the **Overworld**.
2. Reach **Y 310 or higher** in a standard-height world, with unobstructed sky above you. The actual requirement is within ten blocks of the dimension's maximum build height.
3. Wait until midnight: world time **17500–18500**. For testing, `/time set midnight` places the world inside this window.
4. Aim almost straight up, at pitch **−85° to −90°**. Fully draw the bow for at least one second, then release.
5. Read the **Falling Star coordinates** in chat or the bow's tooltip. The ritual consumes one arrow in Survival.
6. Travel to those coordinates with the same dormant bow. End Rod particles mark the location when you approach.
7. Stand within **six blocks** of the destination's block center and right-click with the bow. It becomes **Eventide**.

The ritual selects a surface location in loaded terrain within 96 blocks along each horizontal axis. If no suitable loaded location is found, it asks you to try again. There is no separate star item to collect.

### Abilities

- **Astral lances:** A fully charged ability arrow hitting an entity calls five area strikes after a default two-second delay.
- **Constellation:** Ability hits track up to three distinct targets. Once three are linked, subsequent links damage the other marked targets within 64 blocks. The list expires after eight seconds without a new link.
- **Final judgment:** The fifth lance adds damage based on the victim's maximum health. Repeat hits on an already linked target build Alignment, strengthening this bonus.
- **Cooldown:** Special shots share a default **four-second cooldown**. Ordinary shots do not trigger the special effects.

---

## Worldeater — Heart of the Wyrm

The **Heart of the Wyrm** is a crafting ingredient for Worldeater. It is separate from Eventide's celestial components.

### Craft and bind the Wyrm Effigy

1. Craft **Dragon's Breath + Nether Star + End Crystal + Echo Shard** in any arrangement to make a **Wyrm Effigy**.
2. Defeat the original Ender Dragon and earn the vanilla dragon-kill advancement.
3. In the End, right-click a **Dragon Egg** with the effigy. This records the egg without consuming it.
4. Place a **Dragon Head directly above a Respawn Anchor** in the End. Right-click the head with that same effigy to bind the hunt. The anchor does not need charging or activation.
5. Respawn the Ender Dragon using End Crystals.
6. Reduce the rematch dragon to **20% health or less**. While it is perched, hit it with an arrow while holding a **vanilla bow** in either hand. Keep the bound effigy in your inventory.
7. The effigy is consumed and the **Heart of the Wyrm** is added to your inventory, or dropped if your inventory is full. Each dragon can award one heart through this mechanic.

Craft **Heart of the Wyrm + vanilla Bow + Dragon's Breath** in any arrangement to obtain **Worldeater**.

### Abilities

- **Guided projectile:** Fully charged ability arrows gently steer toward valid nearby targets ahead of them and destroy nearby arrows belonging to other shooters.
- **Dragon dive:** Impact creates a dragon-breath trail from the firing position to the hit location, damaging, launching and igniting enemies along it. The trail is capped at 64 blocks.
- **Dragonfire:** Impact leaves a damaging zone for **five seconds** by default, dealing damage once per second.
- **Dominance:** Kills credited to this bow build charges. At **10 charges**, the next fully charged ability shot consumes them, doubles dive and dragonfire damage, and expands the dragonfire radius from **three to seven blocks**.
- **Cooldown:** Special shots have a default **four-second cooldown**.

Dragon effects use Minecraft particles and sounds; no custom dragon model is required.

---

## Godsplitter — Blunted Edge Trials

Find **Blunted Edge** through Ancient City chest loot or the vanilla Trial Chamber reward loot table. Its additional loot chance is **18%** by default, controlled by `bluntedEdgeChance`.

Complete these trials in order with the same bow:

1. Kill a hostile enemy from **at least 100 blocks** away, measured from the firing position.
2. Kill **three distinct hostile enemies with three consecutive shots**.
3. Kill a hostile enemy while **both you and the target are airborne**, at the time of the kill. Your shot must also have been fired while airborne.

Completing the final trial consumes Blunted Edge and awards **Godsplitter**.

### Abilities

- Fully draw, then keep your aim steady for another **1.5 seconds** until **PERFECT SHOT** appears.
- Perfect arrows travel without gravity and can hit up to **five entities**.
- Hits in the upper quarter of a target's body count as headshots and use a default **3× damage multiplier**.
- Perfect hits normally deal **75% armor-bypassing damage**. Five consecutive perfect hits against the same target raise that to **100%**.
- Perfect headshots execute non-boss targets at **15% maximum health or less**.
- An imperfect release or an arrow missing its target resets armor-severance progress.

---

## 🎨 Custom Tooltip Art

All items in the mod's namespace receive decorative tooltip art. Designs are assigned by item theme rather than a gameplay tier system.

The supplied themes include common, rare, epic, legendary, mythic, collectible, cosmetic and nature-themed art. Legendary and mythic animated variants are used on selected bows. Eventide, Worldeater and Godsplitter use the animated mythic design.

Tooltip art does not alter item stats, loot probabilities or progression requirements.

---

## 🔮 Bows with Special Resources

Only these bows require extra materials or energy:

| Bow                 | Resource          |
| ------------------- | ----------------- |
| **Aurora’s Grace**  | Rift Shards       |
| **Dusk Reaper**     | Soul Fragments    |
| **Radiance**        | Experience Points |
| **Crimson Nexus**   | Player Health     |
| **Ethereal Hunter** | Hunger Points     |

---

## 📊 Attributes & Equipment System (v3.0+)

Starting in **Too Many Bows 3.0.0**, bows can now scale using custom attributes and equippable trinkets.  
These systems allow for deeper build customization and future addon compatibility.

---

## 🧬 New Attributes

These attributes apply to players and modify bow performance dynamically.

| Attribute                   | Description                                                             |
|-----------------------------|-------------------------------------------------------------------------|
| **Bow Draw Speed**          | Controls how quickly bows can be drawn. Higher values reduce draw time. |
| **Bow Damage**              | Multiplies the base damage of bow projectiles.                          |
| **Bow Critical Hit Chance** | Affects the ability to hit a Critical shot ( more damage ).             |

> These attributes are used internally by bows and can be modified by trinkets, equipment, or addon mods.

---

## 🧿 Trinkets & Curio Items

Trinkets are equippable items that grant passive bonuses when worn.  
All trinkets in **Too Many Bows** interact with the new attribute system.

![trinkets](https://i.imgur.com/49XCQLg.png)

| Trinket | Effect |
| ------ | ------ |
| **Dead Eye’s Pendant** | Increases critical hit chance. *(Stackable)* |
| **Windwoven Gloves** | Increases bow draw speed. *(Stackable)* |
| **Fletcher’s Talisman** | Reduces durability loss on bows. |
| **Sharpshot Ring** | Increases bow damage. *(Stackable)* |
| **Stormbound Signet** | Provides a stronger bow damage bonus. *(Stackable)* |

### Acquisition
- All trinkets are obtained through the **loot system**.
- Trinkets are equipped via the **Curios / Trinkets** equipment interface.

---

## ⚙️ Configuration System

Starting in **Too Many Bows 3.0.0**, nearly every aspect of the mod is configurable via JSON files.  
Configs are split into three categories: **bow configs**, the **loot config**, and the **accessories config**.

---

## 📁 Config File Locations

All config files are generated automatically on first launch and can be edited freely.

| Config Type | Path |
| --- | --- |
| **Bow Configs** | `config/too_many_bows/bows/<bow_name>.json` |
| **Relic Bow Config** | `config/too_many_bows/bows/forbidden_three.json` |
| **Loot Config** | `config/too_many_bows.json` |
| **Accessories Config** | `config/too_many_bows/accessories/` |

> Missing config files are generated with defaults. Bow JSON loading falls back to defaults if parsing fails; it does not automatically rewrite every malformed file. Back up files before editing.

---

## 🏹 Bow Config Fields

Most configurable bows have their own JSON file inside the `bows/` folder. Eventide, Worldeater and Godsplitter share `forbidden_three.json`. While every bow is unique, they share common categories of fields:

| Field Category | Examples | Description |
| --- | --- | --- |
| **Damage** | `direct_hit_damage`, `direct_hit_damage_override` | Controls base projectile damage. Set to `-1.0` to leave unchanged. |
| **Projectile Behavior** | `max_lifetime_ticks`, `allow_pickup`, `discard_after_entity_hit` | Controls arrow lifetime, pickup rules, and hit behavior. |
| **Spread & Multishot** | `arrow_count`, `spread_angle_degrees`, `velocity_multiplier` | Controls how many arrows fire and their spread pattern. |
| **Ability Effects** | `burst_radius`, `target_levitation_duration_ticks`, `owner_slow_falling_enabled` | Toggles and tunes each bow's unique ability. |
| **Trail Particles** | `trail_particles_enabled`, `trail_particles_per_tick`, `trail_particle_offset_y` | Controls the particle trail on arrows in flight. |
| **Hit/Burst Particles** | `burst_particles_enabled`, `burst_particle_count`, `burst_particle_offset_x` | Controls particles spawned on impact. |
| **Sounds** | `shoot_sound_enabled`, `burst_sound_volume`, `burst_sound_pitch` | Controls volume and pitch of firing and impact sounds. |
| **Armor Penetration** | `default_armor_penetration_factor`, `min/max_armor_penetration_factor` | Controls how much armor is bypassed (e.g. Ancient Sage Bow). |
| **Ricochet** | `starting_ricochet_count`, `max_ricochets`, `ricochet_velocity_multiplier` | Controls bounce behavior (e.g. Astral Bound). |

> Not all fields apply to every bow — each bow's JSON only contains fields relevant to its mechanics.

**Example snippet** — `aethers_call.json`:
```json
{
  "direct_hit_damage": 6.0,
  "burst_radius": 4.0,
  "target_levitation_enabled": true,
  "target_levitation_duration_ticks": 40,
  "trail_particles_enabled": true,
  "trail_particles_per_tick": 1
}
```

---


## 🌠 Relic Bow Configuration

The shared file is **`config/too_many_bows/bows/forbidden_three.json`**. In development, look under your launch's `run/config/` folder. The values below are the current source defaults; existing configs can override them.

| Field | Default | Purpose |
| --- | --- | --- |
| `eventideDamage` | `14.0` | Eventide arrow damage setting |
| `lanceDamage` | `8.0` | Eventide lance damage and constellation-link damage |
| `finalMaxHealthFraction` | `0.08` | Extra maximum-health fraction on the final lance, before Alignment bonuses; extra damage is capped at 100 |
| `worldeaterDamage` | `18.0` | Worldeater arrow damage setting |
| `dragonDiveDamage` | `16.0` | Base dragon-dive damage |
| `dragonfireDamage` | `4.0` | Dragonfire damage per pulse |
| `godsplitterDamage` | `20.0` | Godsplitter arrow damage setting |
| `headshotMultiplier` | `3.0` | Godsplitter perfect-headshot multiplier |
| `abilityCooldownTicks` | `80` | Eventide and Worldeater special-shot cooldown |
| `maxDominance` | `10` | Charges required for an empowered Worldeater shot |
| `starfallDelayTicks` | `40` | Delay before Eventide's first lance; clamped to 10–100 ticks |
| `dragonfireDurationTicks` | `100` | Dragonfire-zone duration; clamped to 20–180 ticks |
| `fragmentChance` | `0.25` | Celestial component chance in its matching chest |
| `bluntedEdgeChance` | `0.18` | Additional Blunted Edge loot chance |
| `affectPlayers` | `false` | Allows relic targeting/effects against players |
| `protectPets` | `true` | Excludes tamed animals from relic targeting/effects |

**20 ticks = one second.** Actual arrow damage also depends on flight speed, critical hits, enchantments and attributes.

For a stronger Eventide, these are suggested starting values rather than shipped defaults:

```json
"eventideDamage": 24.0,
"lanceDamage": 12.0,
"finalMaxHealthFraction": 0.12
```

Edit those fields inside the existing JSON object. Use `/tmb reload bows` for combat-value changes. **Restart the game/server after changing `fragmentChance` or `bluntedEdgeChance`**, because their loot pools are built during initialization. Loot changes do not reroll chests whose contents have already generated.

---

## 🎁 Loot Config Fields

The loot config lives at `config/too_many_bows.json` and controls which bows and trinkets appear in world loot, at what rates, and in which chests.

| Field | Default | Description |
| --- | --- | --- |
| `easyLootEnabled` | `true` | Enables easy-tier loot injection |
| `easyLootDropChance` | `0.5` | Drop chance for easy loot (0.0–1.0) |
| `mediumLootEnabled` | `true` | Enables medium-tier loot injection |
| `mediumLootDropChance` | `0.4` | Drop chance for medium loot (0.0–1.0) |
| `hardLootEnabled` | `true` | Enables hard-tier loot injection |
| `hardLootDropChance` | `0.3` | Drop chance for hard loot (0.0–1.0) |
| `endgameLootEnabled` | `true` | Enables endgame-tier loot injection |
| `endgameLootDropChance` | `0.2` | Drop chance for endgame loot (0.0–1.0) |
| `globalBowPullSpeed` | `16.0` | Global multiplier for bow draw speed |
| `easyLootTables` | `[simple_dungeon, mineshaft]` | Chest loot tables to inject easy items into |
| `easyLootItems` | *(list of item IDs)* | Items that can appear in easy loot |
| `mediumLootTables` / `mediumLootItems` | — | Same as above for medium tier |
| `hardLootTables` / `hardLootItems` | — | Same as above for hard tier |
| `endgameLootTables` / `endgameLootItems` | — | Same as above for endgame tier |

### Default Loot Tiers

| Tier | Default Chest Sources |
| --- | --- |
| **Easy** | Simple Dungeon, Abandoned Mineshaft |
| **Medium** | Jungle Temple, Pillager Outpost, Mineshaft, Dungeon |
| **Hard** | Stronghold Corridor, Nether Fortress, Bastion Treasure |
| **Endgame** | End City, Nether Fortress, Bastion Treasure |

> Drop chances are clamped between `0.0` and `1.0`. Invalid values are automatically corrected on load.  
> A timestamped backup of your loot config (e.g. `too_many_bows.json.bak-20250101-120000`) is created automatically before any migration or correction is applied.

---

## 🧿 Accessories Config Fields

The accessories config controls the stat bonuses granted by each trinket.

| Field | Default | Trinket |
| --- | --- | --- |
| `deadEyesPendantCritBonus` | `0.08` | **Dead Eye's Pendant** — critical hit chance bonus per stack |
| `drawSpeedGloveBonus` | `0.75` | **Windwoven Gloves** — bow draw speed bonus per stack |
| `sharpshotRingBonus` | `0.15` | **Sharpshot Ring** — bow damage bonus per stack |
| `stormboundSignetBonus` | `0.30` | **Stormbound Signet** — stronger bow damage bonus per stack |

> Stackable trinkets multiply their bonus by the number equipped. The **Fletcher's Talisman** reduces durability loss and has no numeric config value.

---

## 🖥️ Commands

All **Too Many Bows** commands require **operator permission level 2** or higher.

| Command | Description |
| --- | --- |
| `/tmb reload` | Reloads **all** configs (bows, loot, and accessories) |
| `/tmb reload all` | Alias for the above — reloads everything |
| `/tmb reload bows` | Reloads only bow JSON configs from disk |
| `/tmb reload loot` | Reloads only the loot config (`too_many_bows.json`) |
| `/tmb reload accessories` | Reloads only the accessories/trinket configs |

> Registered bow combat settings can be reloaded with `/tmb reload bows`, including `forbidden_three.json`. Relic chest-loot probabilities require a restart. Reloading does not reroll already generated chest contents.

---

## 🧩 Addon & Modding Support

The attribute system introduced in v3.0.0 allows other mod developers to:
- Create addon mods that add new trinkets
- Introduce new attribute-scaling bows
- Extend **Too Many Bows** without modifying its core code

This system is designed to be expandable and future-proof.

---

## 💎 Power Crystal – Bow Repair

The **Power Crystal** is used to repair bows from this mod.
![crystal](https://i.imgur.com/0HsufdW.png)

### How to Repair Bows

1. Open an **Anvil**
2. Place your damaged bow in the first slot
3. Place a **Power Crystal** in the second slot
4. Take your repaired bow from the output slot
   ![repair](https://i.imgur.com/utz34dd.png)
   **Tooltip reference:**

> _A crystal of pure energy._  
> _Use in an anvil to repair bows._

---

## ⚡ Special Ammo & Fuel Items

| Item                  | Purpose                         |
| --------------------- | ------------------------------- |
| **Rift Shard**        | Used to fire **Aurora’s Grace** |
| **Soul Fragment**     | Used to fire **Dusk Reaper**    |
| **Power Crystal**     | Used to repair bows             |
| **Player Health**     | Consumed by **Crimson Nexus**   |
| **Experience Points** | Consumed by **Radiance**        |
| **Hunger**            | Consumed by **Ethereal Hunter** |

---

## 🌙 Notes

- Some older bow mechanics still need fuller documentation; entries marked as undocumented should not be treated as confirmed ability descriptions.
- Eventide, Worldeater and Godsplitter use a one-second full draw for their relic mechanics.
- The acquisition instructions here describe the current Minecraft 1.21.1 source; older releases may differ.
- Bows without listed special ammo use **normal arrows**.
- More bows, synergies, and enchantments will be added as the mod evolves.
