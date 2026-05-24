# Web Regression Checks

Use this checklist for web parity fixes that are difficult to cover through the JVM test suite alone. Run the automated checks first, then use the browser checks for canvas, TeaVM, and real rendering behavior.

## Automated

- `./gradlew :core:test`
  - Covers key-bound `Button` repeat handling.
  - Covers legacy `LevelTransition.destType` restore defaults.
  - Covers destination-cell selection avoiding entrance fallback when matching exit terrain exists.
- `./gradlew :web:compileJava :web:buildWebBundle --rerun-tasks`
  - Rebuilds the TeaVM output so generated `dungeon.js` cannot be stale.
- `git diff --check`
  - Catches whitespace and patch hygiene issues before review.
- `rg -n "animation probe|animationProbe|probe mob|R004 transition probe|transitionProbeStage|runTransitionProbe|triggerTransitionProbe|R005|r005SpecialRoomProbe|runR005|R005WebTrace|R007|r007visual|R007VisualProbe|restore_switch_queued|levelHang|lvlHang|R008" core/src/main/java web/src/main/java web/src/main/resources web/build/dist/webapp`
  - Should return no production or generated probe strings after temporary diagnostics are removed.

## Browser

- Startup: load `web/build/dist/webapp` through a local HTTP server, confirm the canvas focuses and the title or game scene renders without browser warnings/errors.
- Action animation: in a live run, trigger an attack or toolbar action and confirm the sprite remains visible for at least one rendered action frame.
- Keyboard input: with the canvas focused, verify number-row quickslot keys reach the game, `E` search/examine triggers once per physical hold, and settings remapping persists after reload.
- Resume movement: if `C` is expected to continue movement, first remap `Resume Motion` to `C` in settings; default desktop/web binding remains `R`.
- Level transition: descend and ascend through a regular stair pair and confirm ascending returns to the previous level's exit cell, not its entrance.
- Special rooms: if crystal/frame-wall disappearance is reported again, reproduce an exact level down/up path and classify state before rendering. Compare `Level.map`, relevant mob counts, `DungeonWallsTilemap.skipCells`, camera/visibility, and screenshots before patching.

## Processing-State Race Stress

Use this only when a web hang, stale save, or stale special-room rendering report suggests actor work was still in flight during pause, save, or scene teardown. Do not use it as a reason to weaken `GameScene.waitForActorThread(...)`; first distinguish a parked actor thread from a genuinely processing actor.

- Record `Actor.processing()`, `Actor.current`, `Dungeon.hero.ready`, `Dungeon.hero.curAction`, `Game.switchingScene()`, `InterlevelScene.mode`, depth/branch, and the return value of `GameScene.waitForActorThread(...)`.
- On web lifecycle tests, distinguish `visibilitychange:hidden` from `pagehide`: gdx-teavm calls app `pause()` for hidden visibility, then calls `pause()` plus `dispose()` for pagehide.
- Cover a normal stair transition from the hero actor path.
- Cover a non-stair transition if reachable, such as chasm fall, fadeleaf, passage scroll, beacon, or escape crystal.
- Cover an action with a pending sprite callback, such as attack, operate, or jump, then trigger scene teardown before the callback completes.
- Cover a movement sprite wait where `CharSprite.isMoving` is true and the actor thread is waiting on the sprite monitor. Classify this separately from the parked-thread wait; it means `Actor.processing()` is true.
- Cover browser pause/page-hide/reload while the hero is not ready, then inspect the saved level and resumed game state.
- Watch for old-scene `GameScene.updateMap(...)`, `sprite.parent` VFX, or tall-sprite `DungeonWallsTilemap.skipCells` mutations after `Dungeon.level` or `GameScene.scene` changes.
- For `Game.runOnRenderThread(...)`, tag the scene/depth/level identity when the callback is posted and when it runs. gdx-teavm drains `postRunnable` callbacks before each frame's render/step, so stale-callback claims need identity evidence, not timing assumptions.
- Before patching, classify the result as processing-state hang, correct wait, stale save, stale render-only state, or unrelated rendering/input failure.
