# TeaVM Web Issue Investigation

This note records five web-only parity bugs found while testing the TeaVM build. They looked like ordinary game-state, item, talent, or UI bugs at first, but the shared pattern was backend-specific behavior in generated JavaScript: Java reflection metadata, enum restoration, enum-key identity, and formatter argument handling.

## Scope

- Target: TeaVM web build.
- Affected area: saved/restored game state, level transitions, subclass and talent gameplay gates, scene teardown timing, and message-resource formatting.
- Non-goal: repairing already-corrupted saves. The fixes prevent new bad state and keep load/save paths from crashing, but they do not reconstruct missing objects from old saves.

## Bug 1: Toxic Gas Disappears From Toxic Gas Rooms

### Symptom

In the web build, a toxic gas special room could appear without its gas after saving, loading, or switching scenes. The room itself was still restored, but the gas-producing blob was missing.

Representative web parity logs showed the level still knew about `ToxicGasRoom`, but the `ToxicGas`/seed blob state was empty:

```text
roomClasses=...,ToxicGasRoom,...
blobs=3[...,ToxicGas{volume=0 area=0,0-0,0 activeCells=0 sample=[]}]
```

The save/load noise made this easy to misread as a room restore problem, but the room list was intact. The missing state was the nested blob object used to seed or drive the room gas.

### Root Cause

The game stores many objects through `Bundle.put(String, Collection<? extends Bundlable>)`. That path used reflection metadata to skip non-static member classes:

```java
if (object != null && (!Reflection.isMemberClass(object.getClass()) || Reflection.isStatic(object.getClass()))) {
    ...
}
```

That rule is valid on JVM backends because true non-static inner classes require an owning instance and cannot be restored with a no-arg constructor.

In TeaVM output, some static nested classes can be reported like non-static member classes by the libGDX reflection wrapper. That caused valid static nested `Bundlable` classes to be skipped during serialization. `ToxicGasRoom.ToxicGasSeed` was one concrete casualty.

This was not toxic-gas-specific. Other static nested `Bundlable`/`Blob` classes use the same save path, such as:

- `MagicalFireRoom.EternalFire`
- `WeakFloorRoom.WellID`
- `CavesBossLevel.PylonEnergy`
- `Tengu.FireBlob`
- `Tengu.ShockerBlob`
- `Room.Door`

### Fix

The bundle serialization rule now checks the capability the restore path actually needs: whether the class has a default constructor.

- `Reflection.hasDefaultConstructor(Class)` was added as the backend-neutral predicate.
- `Bundle.get()` restores a `Bundlable` only when the class resolves and has a default constructor.
- `Bundle.put(String, Collection<? extends Bundlable>)` uses the same default-constructor rule.
- Direct and collection `Bundlable` serialization share the same `putBundlable(...)` helper.

This preserves the old intent:

- static nested classes with no-arg constructors are saved/restored;
- true owner-bound non-static inner classes are skipped because they cannot be restored generically;
- the code no longer depends on TeaVM's static/member-class metadata.

### Regression Coverage

`BundleStaticMemberClassTest` covers:

- direct restore of `ToxicGasRoom.ToxicGasSeed`;
- collection restore of `ToxicGasRoom.ToxicGasSeed`, `MagicalFireRoom.EternalFire`, and `Room.Door`;
- blob cell/volume preservation after collection restore;
- skipping a true non-static inner `Bundlable`.

## Bug 2: Ascending Immediately Returns Hero To Previous Level Entrance

### Symptom

In the web build, the hero could descend a regular stair and immediately ascend, but appear at the previous level's entrance instead of the previous level's exit.

Representative logs:

```text
activateTransition mode=ASCEND depth=3 branch=0 heroPos=457 transitionType=REGULAR_ENTRANCE transitionCell=457 destDepth=2 destBranch=0 destType=REGULAR_EXIT
...
transitions=2[REGULAR_ENTRANCE@679->1/0/REGULAR_EXIT,REGULAR_EXIT@281->3/0/REGULAR_ENTRANCE]
...
destinationCell fallback cell=679 destType=REGULAR_EXIT fallbackTransition=REGULAR_ENTRANCE@679->1/0/REGULAR_EXIT
```

The log said the desired destination type was `REGULAR_EXIT`, and the loaded level clearly had `REGULAR_EXIT@281`, yet lookup fell through to the entrance fallback at `679`.

### Root Cause

The transition lookup path used enum identity checks for `LevelTransition.Type`. On JVM this is normally safe, but TeaVM-generated enum values restored from save data showed identity fragility: values logged with the same name did not reliably match by identity in transition logic.

The older `Bundle.getEnum(...)` implementation used `Enum.valueOf(...)`. The observed behavior indicated that, in the TeaVM build, restored enum values could still behave differently from the canonical constants used by code-level comparisons.

The fragile identity checks affected more than one method:

- `Level.getTransition(...)`
- `Level.getTransitionExact(...)`
- `Level.terrainTransitionCell(...)`
- `Level.activateTransition(...)`
- transition-dependent special logic in `Hero`, `SewerLevel`, `MiningLevel`, `CityLevel`, `CavesLevel`, and `HallsBossLevel`

### Fix

Enum restore was hardened at the deserialization boundary.

`Bundle.getEnum(...)` now canonicalizes all restored enums by name through `Reflection.canonicalEnum(...)`. The helper first resolves the public static enum field, then falls back to scanning `getEnumConstants()`:

```java
Object value = ClassReflection.getField(enumClass, name).get(null);
...
for (E value : enumClass.getEnumConstants()) ...
```

This applies to every enum restored through `Bundle`, not only level transitions. Once restored values are canonical, transition game logic can keep the original `==`, `!=`, and `switch` semantics instead of carrying name-based match helpers throughout gameplay code.

For the failing stair case, the expected result after the fix is:

```text
destinationCell exact cell=281 destType=REGULAR_EXIT
```

or, if transition metadata is absent but terrain is intact:

```text
destinationCell terrain cell=281 destType=REGULAR_EXIT
```

The bad signal is:

```text
destinationCell fallback cell=679 destType=REGULAR_EXIT
```

### Regression Coverage

Focused tests cover:

- destination-cell exact transition lookup;
- terrain fallback when transition metadata is missing;
- legacy `destType` default restoration;
- switch-level placement;
- load-level identity checks;
- canonical enum restore from `Bundle.getEnum(...)`.

## Bug 3: Berserker Rage Does Not Increase After Damage

### Symptom

In the web build, a Berserker could take normal physical damage without the rage value increasing. The hero still appeared to be a Berserker, but the subclass-specific rage gain did not run.

The relevant gameplay path is:

```text
incoming physical damage
-> Hero.defenseProc(...)
-> if Berserker, Buff.affect(hero, Berserk.class).damage(damage)
```

When the gate failed, the `Berserk` buff was never created or updated, so the UI correctly showed no rage increase.

### Root Cause

This was the same enum-identity class of bug as the stair transition issue, but on `HeroSubClass` instead of `LevelTransition.Type`.

The rage gate used enum identity:

```java
if (damage > 0 && subClass == HeroSubClass.BERSERKER) {
    ...
}
```

`Hero.subClass` is restored through `Bundle.getEnum(...)`. In TeaVM, restored enum values can have the correct `name()` while still failing identity checks against the generated static enum constants. That means the UI and save data can still say `BERSERKER`, while gameplay code using `== HeroSubClass.BERSERKER` can skip Berserker-only behavior.

The earlier enum audit missed this because it generalized the stair bug only to `LevelTransition.Type`. The correct audit boundary is broader: any enum restored from `Bundle` and then used in gameplay logic should avoid relying solely on identity when TeaVM is a target.

### Fix

`Bundle.getEnum(...)` now first resolves the enum constant by its public static enum field name before falling back to the enum constants array. That returns the same object used by ordinary code-level constants, so gameplay gates such as `subClass == HeroSubClass.BERSERKER` can remain unchanged.

The enum audit was then re-centered on restore/input boundaries instead of gameplay callsites:

- `HeroClass`, `HeroSubClass`, `LevelTransition.Type`, room/door/state enums, alignments, and item augments are canonicalized through `Bundle.getEnum(...)`.
- Direct enum-name restore/input sites that previously called `valueOf(...)`, such as badges, heaps, journal landmarks, toolbar mode, and news icons, use the same `Reflection.canonicalEnum(...)` helper.
- `Talent` map restore canonicalizes persisted talent names before writing `Hero.talents` and `Hero.metamorphedTalents`.
- Broad helper methods such as `HeroSubClass.matches(...)`, `HeroClass.matches(...)`, `Talent.matches(...)`, `Char.Alignment.matches(...)`, augment matchers, and `LevelTransition.typeMatches(...)` were removed so gameplay code keeps normal enum semantics.

### Regression Coverage

Focused tests cover:

- restored-Berserker-style rage gating through `Hero.defenseProc(...)`;
- subclass gates after canonical enum restore;
- canonical enum restore through `Bundle.getEnum(...)`.

## Bug 4: Shard Of Oblivion Info Crashes The Web Build

### Symptom

In the web build, opening Shard of Oblivion item info could crash with a TeaVM JavaScript runtime error:

```text
Fatal Error: RuntimeException (JavaScript) TypeError: Cannot read properties of undefined (reading 'constructor')
...
at ju_Formatter$FormatWriter_formatDecimalInt
at jl_String_format
at cssm_Messages_format
at cssit_ShardOfOblivion_statsDesc
at cssw_WndInfoItem_fillFields
```

The item logic was not the source of the crash. `ShardOfOblivion.statsDesc()` passed one numeric argument through `Messages.get(...)`, but the localized message text contained both a literal percent and a later numeric placeholder:

```properties
items.trinkets.shardofoblivion.stats_desc=... 20%% ... _%d item(s)_ ...
```

### Root Cause

On the JVM, `%%` is a literal percent and does not consume a formatter argument. In the TeaVM-generated formatter path, this shape behaved as if the literal percent advanced argument selection. The later `%d` then tried to format a missing second argument, which surfaced in generated JavaScript as `undefined.constructor`.

This was broader than Shard of Oblivion. A scan found many message-resource lines where a literal `%%` appeared before a later unindexed formatter conversion such as `%d` or `%s`. Those strings could be safe on JVM while remaining crash-prone on TeaVM.

### Fix

`Messages.format(...)` now normalizes implicit formatter arguments before calling `String.format(...)`. It rewrites formatter tokens such as `%d`, `%s`, and `%.1f` to explicit JVM-equivalent argument indexes such as `%1$d`, `%1$s`, and `%1$.1f`, while preserving `%%`, `%n`, already indexed tokens, and relative `%<` tokens.

This keeps translated resources readable and avoids broad `.properties` churn while ensuring TeaVM receives explicit argument indexes for strings that contain a literal `%%` before later formatter arguments.

`Messages.format(...)` also catches `RuntimeException`, reports it, and returns the raw format text. That is a defensive fallback only: the primary fix is to make resource strings formatter-safe before they reach TeaVM.

### Regression Coverage

`MessageFormatNormalizerTest` covers literal-percent-before-argument normalization, sequential implicit argument ordering, flags, width, precision, date/time conversions, escaped percent signs, newlines, already indexed tokens, and relative `%<` tokens.

Focused verification:

```sh
./gradlew :core:test --tests com.shatteredpixel.shatteredpixeldungeon.messages.MessageFormatNormalizerTest
./gradlew :web:test
./gradlew :web:buildWebBundle
```

## Bug 5: Strongman Talent Shows Points But Does Not Increase Strength

### Symptom

In the web build, a level 20 Berserker could show the tier-3 `Strongman` talent as fully upgraded while the hero info panel still showed the unmodified strength value. The concrete report showed a Berserker at strength 15 with the tier-3 talent pips filled. For a Warrior/Berserker with base strength 15 and three points in `Strongman`, the displayed strength should compute:

```text
15 + floor(15 * (0.03 + 0.05 * 3)) = 17
```

The UI could still render the filled talent pips because it iterated the stored talent map directly and passed each existing key/value pair into the talent button. Gameplay logic such as `Hero.STR()` queried the same map by `Talent.STRONGMAN`, and that query returned zero points.

### Root Cause

This was another enum-identity failure, this time for `Talent` keys stored inside `Hero.talents`.

The old lookup logic was identity-only:

```java
for (Talent f : tier.keySet()) {
    if (f == talent) return tier.get(f);
}
```

That is safe only if every `Talent` reference is the same generated object. In TeaVM, restored or UI-routed enum values can have the same `name()` while failing identity checks. The result was split-brain state:

- UI: displays points from the map entry it already has.
- Logic: asks for `Talent.STRONGMAN`, fails `==`, and gets zero.

The first enum audit missed this because it focused on enum fields restored directly through `Bundle.getEnum(...)`. `Hero.talents` is different: the enum is used as a map key and is also passed through UI callbacks and metamorphosis replacement maps.

### Fix

The fix moved the name-stable behavior to the restore boundary. `Talent.restoreTalentsFromBundle(...)` canonicalizes persisted talent names before writing:

- `Hero.metamorphedTalents` replacement keys and values;
- restored tier point entries in `Hero.talents`.

For tier maps, restore first initializes the normal class/subclass/armor talent keys, then resolves saved names back to those existing keys before writing point values. The important behavioral point is that gameplay code such as `Hero.pointsInTalent(...)`, `Hero.upgradeTalent(...)`, `Talent.onTalentUpgraded(...)`, and UI callbacks can keep ordinary enum identity logic because the stored keys are canonical again.

### Regression Coverage

`HeroSubClassTest.strongmanTalentIncreasesStrength` verifies that three points in `Strongman` are readable through `Hero.pointsInTalent(...)` and increase a 15-strength Warrior/Berserker to 17 strength. `HeroSubClassTest.restoredStrongmanPointsUseCanonicalTalentKey` verifies the restore path that produced the web-only split-brain talent state.

## Related Race Hardening

The gas investigation also exposed a separate web risk: scene teardown, save, or level switch can happen while the actor thread is still processing. That is distinct from the static nested class bug, but it can make transient state look empty during save.

Two hardening changes cover this class:

- `GameScene` does not treat a still-processing actor thread as safely stopped on web.
- `Blob.storeInBundle(...)` serializes active `cur` cells even during the mid-tick window where `volume` has temporarily reached zero.

These changes are still useful after the bundle fix because they protect live mid-action state, not just static nested class serialization.

## Audit Follow-Ups

The line-by-line Java audit found two broader hardening patterns beyond the original three web bugs.

First, translated formatter strings can still be genuinely malformed independently of the TeaVM literal-percent bug. A Swedish Runic Blade ability description had `%d%%%`, which leaves a dangling formatter percent even on the JVM. That string now uses the same one-argument `%d%%` shape as the other locales. The broader TeaVM formatter edge case is handled in `Messages.format(...)`, not by rewriting all resource strings.

Second, direct single-cell helpers should guard map bounds themselves. Generated levels normally keep gameplay cells away from unsafe borders, but web rendering, scene refresh, and unusual test levels can call direct update paths for edge cells. Plant, trap, scene/tile, and wall-blocking update paths now fail closed with `insideMap` or equivalent cell checks before neighbor reads.

## Verification

Run the focused JVM tests:

```sh
./gradlew :core:test \
  --tests com.shatteredpixel.shatteredpixeldungeon.messages.MessageFormatNormalizerTest \
  --tests com.watabou.utils.BundleStaticMemberClassTest \
  --tests com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelSceneDestinationCellTest \
  --tests com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransitionRestoreTest \
  --tests com.shatteredpixel.shatteredpixeldungeon.DungeonSwitchLevelPlacementTest \
  --tests com.shatteredpixel.shatteredpixeldungeon.DungeonLoadLevelIdentityTest \
  --tests com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClassTest \
  --tests com.shatteredpixel.shatteredpixeldungeon.scenes.GameSceneActorThreadWaitTest \
  --tests com.watabou.noosa.GameSceneSwitchReadinessTest \
  --tests com.shatteredpixel.shatteredpixeldungeon.plants.PlantBoundsTest \
  --tests com.shatteredpixel.shatteredpixeldungeon.levels.traps.TrapBoundsTest \
  --tests com.shatteredpixel.shatteredpixeldungeon.scenes.GameSceneCellInspectionTest \
  --tests com.shatteredpixel.shatteredpixeldungeon.tiles.WallBlockingTilemapBoundsTest
```

Then rebuild all relevant Java targets:

```sh
./gradlew :desktop:compileJava :ios:classes :web:buildWebBundle
```

For web manual testing, serve `web/build/dist/webapp` and capture `[WEB-PARITY]` logs around:

- `activateTransition`
- `descend` / `ascend`
- `loadLevel`
- `destinationCell`
- `switchLevel placement`
- `level store` / `level restore complete`
- `regularLevel store rooms` / `regularLevel restore rooms`
- `create blobs`
- `addBlobSprite`

## Investigation Lessons

- If a TeaVM web log prints enum names that look correct but matching fails, suspect enum identity/canonicalization at restore/input boundaries before patching game rules.
- If a static nested `Bundlable` disappears only on web, inspect constructor availability and generated reflection metadata separately.
- If TeaVM crashes inside `String.format` or `Messages.format`, inspect both the resource string shape and the runtime formatter normalizer before changing item or UI logic.
- Prefer capability checks used by the restore path, such as default constructor availability, over Java metadata checks that can drift between backends.
- Avoid save repair until the new-save path is proven correct; repair logic can hide the root cause and make future corrupted states harder to classify.
