---
title: Too Many Bows
description: "Too Many Bows Wiki"
icon: bow
---

# Too Many Bows
![bamf](https://media.forgecdn.net/attachments/1017/632/shulker.png)
This page documents every bow currently present in the **Too Many Bows** mod, including abilities, special firing requirements, and how to repair them.

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
| **Demon’s Grasp**      | _(No tooltip info yet)_                                                        | Standard arrows                |
| **Aether’s Call**      | Enemies hit are lifted with Levitation, grants Slow Falling  1.21 only         | Standard arrows                |
| **Spectral Bow**       | _(Not fully documented yet)_                                                   | Standard arrows                |
| **Cyroheart Bow**      | _(No tooltip info yet)_                                                        | Standard arrows                |
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
| **Loot Config** | `config/too_many_bows.json` |
| **Accessories Config** | `config/too_many_bows/accessories/` |

> Config files are created with default values if they don't exist. If a config is malformed or missing fields, the mod will fill in defaults automatically.

---

## 🏹 Bow Config Fields

Each bow has its own dedicated JSON file inside the `bows/` folder. While every bow is unique, they share common categories of fields:

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

> Changes to config files take effect immediately after running the relevant reload command — **no restart required**.

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

- Some bows still show placeholder or WIP tooltips and will be updated as development continues.
- Bows without listed special ammo use **normal arrows**.
- More bows, synergies, and enchantments will be added as the mod evolves.
