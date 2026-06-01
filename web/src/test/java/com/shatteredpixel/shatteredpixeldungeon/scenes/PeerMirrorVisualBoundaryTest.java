/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.scenes;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.multiplayer.WebMultiplayer;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.PeerMirrorSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Group;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class PeerMirrorVisualBoundaryTest {

	private Level previousLevel;
	private Hero previousHero;
	private int previousDepth;
	private int previousBranch;
	private HashMap<String, WebMultiplayer.PeerMirrorState> previousPeerMirrors;

	@Before
	public void setUp() throws Exception {
		previousLevel = Dungeon.level;
		previousHero = Dungeon.hero;
		previousDepth = Dungeon.depth;
		previousBranch = Dungeon.branch;
		previousPeerMirrors = new HashMap<>(peerMirrorMap());
		peerMirrorMap().clear();
		Actor.clear();
		Dungeon.level = new TestLevel(10, 10);
		Dungeon.hero = new Hero();
		Dungeon.hero.pos = 11;
		Dungeon.depth = 1;
		Dungeon.branch = 0;
	}

	@After
	public void tearDown() throws Exception {
		Actor.clear();
		peerMirrorMap().clear();
		peerMirrorMap().putAll(previousPeerMirrors);
		Dungeon.level = previousLevel;
		Dungeon.hero = previousHero;
		Dungeon.depth = previousDepth;
		Dungeon.branch = previousBranch;
	}

	@Test
	public void peerMirrorVisibilityRequiresSameDepthBranchAndFov() throws Exception {
		WebMultiplayer.PeerMirrorState state = peerMirrorState(1, 0, 22);
		Dungeon.level.heroFOV[22] = true;

		assertTrue(GameScene.shouldShowPeerMirror(state));

		state.connected = false;
		assertFalse(GameScene.shouldShowPeerMirror(state));
		state.connected = true;
		state.gameOver = true;
		assertFalse(GameScene.shouldShowPeerMirror(state));
		state.gameOver = false;

		assertFalse(GameScene.shouldShowPeerMirror(peerMirrorState(2, 0, 22)));
		assertFalse(GameScene.shouldShowPeerMirror(peerMirrorState(1, 1, 22)));
		assertFalse(GameScene.shouldShowPeerMirror(peerMirrorState(1, 0, 23)));
		assertFalse(GameScene.shouldShowPeerMirror(peerMirrorState(1, 0, -1)));
		assertFalse(GameScene.shouldShowPeerMirror(peerMirrorState(1, 0, Dungeon.level.length())));
	}

	@Test
	public void peerMirrorSpriteCannotEnterGameplayActorOrInspectionTypes() throws Exception {
		Dungeon.level.heroFOV[22] = true;
		WebMultiplayer.PeerMirrorState state = peerMirrorState(1, 0, 22);

		assertTrue(GameScene.shouldShowPeerMirror(state));
		assertFalse(Char.class.isAssignableFrom(PeerMirrorSprite.class));
		assertFalse(Mob.class.isAssignableFrom(PeerMirrorSprite.class));
		assertTrue(Dungeon.level.mobs.isEmpty());
		assertTrue(GameScene.getObjectsAtCell(22).isEmpty());
		assertTrue(Dungeon.level.passable[22]);
		assertTrue(Actor.findChar(22) == null);
	}

	@Test
	public void sceneSyncRendersMovesHidesAndRemovesVisualMirrorOnly() throws Exception {
		Dungeon.level.heroFOV[22] = true;
		Dungeon.level.heroFOV[23] = true;

		TestGameScene scene = new TestGameScene();
		Group visualMobs = new Group();
		HashMap<String, PeerMirrorSprite> sceneMirrors = new HashMap<>();
		setSceneField(scene, "mobs", visualMobs);
		setSceneField(scene, "peerMirrors", sceneMirrors);

		peerMirrorMap().put("2", peerMirrorState(1, 1, 0, 22));
		peerMirrorMap().get("2").heroClass = HeroClass.MAGE;
		peerMirrorMap().get("2").armorTier = 3;
		peerMirrorMap().get("2").peerBuffs.add(new WebMultiplayer.PeerBuffState(
				"haste", BuffIndicator.HASTE, Buff.buffType.POSITIVE, "", 0.25f));
		scene.syncPeerMirrors();

		assertEquals(1, scene.created.size());
		TestPeerMirrorSprite sprite = scene.created.get(0);
		assertSame(sprite, sceneMirrors.get("2"));
		assertEquals(1, visualMobs.countLiving());
		assertEquals(22, sprite.lastCell);
		assertTrue(sprite.lastShouldShow);
		assertTrue(sprite.visible);
		assertEquals(HeroClass.MAGE, sprite.heroClass);
		assertEquals(3, sprite.armorTier);
		assertEquals(1, sprite.peerBuffs.size());
		assertEquals(BuffIndicator.HASTE, sprite.peerBuffs.get(0).icon);

		peerMirrorMap().put("2", peerMirrorState(2, 1, 0, 23));
		peerMirrorMap().get("2").heroClass = HeroClass.HUNTRESS;
		peerMirrorMap().get("2").armorTier = 4;
		peerMirrorMap().get("2").peerBuffs.add(new WebMultiplayer.PeerBuffState(
				"poison", BuffIndicator.POISON, Buff.buffType.NEGATIVE, "", 0.5f));
		scene.syncPeerMirrors();

		assertEquals("moving a known peer must reuse the same visual sprite", 1, scene.created.size());
		assertSame(sprite, sceneMirrors.get("2"));
		assertEquals(23, sprite.lastCell);
		assertTrue(sprite.lastShouldShow);
		assertEquals(HeroClass.HUNTRESS, sprite.heroClass);
		assertEquals(4, sprite.armorTier);
		assertEquals(1, sprite.peerBuffs.size());
		assertEquals(BuffIndicator.POISON, sprite.peerBuffs.get(0).icon);

		Dungeon.level.heroFOV[23] = false;
		scene.syncPeerMirrors();

		assertEquals(23, sprite.lastCell);
		assertFalse(sprite.lastShouldShow);
		assertFalse(sprite.visible);
		assertTrue(Dungeon.level.mobs.isEmpty());
		assertTrue(GameScene.getObjectsAtCell(23).isEmpty());
		assertTrue(Dungeon.level.passable[23]);
		assertTrue(Actor.findChar(23) == null);

		peerMirrorMap().clear();
		scene.syncPeerMirrors();

		assertTrue(sprite.killed);
		assertTrue(sceneMirrors.isEmpty());
		assertEquals(0, visualMobs.countLiving());
	}

	@Test
	public void watcherTargetVisualSnapsReplayJumpsAndReturnsToIdle() {
		Dungeon.hero.pos = 11;
		TestWatcherTargetSprite sprite = new TestWatcherTargetSprite();
		Dungeon.hero.sprite = sprite;
		Dungeon.level.visited = null;

		assertTrue(GameScene.syncWatcherTargetCell(55));

		assertEquals(55, Dungeon.hero.pos);
		assertEquals(0, sprite.moveCount);
		assertEquals(1, sprite.interruptCount);
		assertEquals(55, sprite.placedCell);
		assertEquals(1, sprite.idleCount);
	}

	@Test
	public void watcherTargetVisualAnimatesAdjacentReplayMovesThenReturnsToIdle() {
		Dungeon.hero.pos = 11;
		TestWatcherTargetSprite sprite = new TestWatcherTargetSprite();
		Dungeon.hero.sprite = sprite;
		Dungeon.level.visited = null;

		assertTrue(GameScene.syncWatcherTargetCell(12));

		assertEquals(12, Dungeon.hero.pos);
		assertEquals(1, sprite.moveCount);
		assertEquals(11, sprite.moveFrom);
		assertEquals(12, sprite.moveTo);
		assertEquals(0, sprite.interruptCount);
		assertEquals(-1, sprite.placedCell);
		assertEquals(1, sprite.idleCount);
	}

	@Test
	public void watcherKillReplayRemovesRestoredMobFromLevelActorsAndVisuals() {
		TestMob mob = new TestMob();
		TestWatcherMobSprite sprite = new TestWatcherMobSprite();
		mob.pos = 44;
		mob.HP = mob.HT = 3;
		mob.sprite = sprite;
		Dungeon.level.mobs.add(mob);
		Actor.add(mob);

		assertSame(mob, Actor.findChar(44));
		assertTrue(Dungeon.level.mobs.contains(mob));

		assertTrue(GameScene.syncWatcherMobKilled(44));

		assertEquals(0, mob.HP);
		assertFalse(Dungeon.level.mobs.contains(mob));
		assertTrue(Actor.findChar(44) == null);
		assertTrue(sprite.killed);
	}

	@Test
	public void watcherKillReplayTreatsAlreadyMissingMobAsSynced() {
		assertTrue(GameScene.syncWatcherMobKilled(44));
		assertTrue(Actor.findChar(44) == null);
	}

	private static WebMultiplayer.PeerMirrorState peerMirrorState(int depth, int branch, int cell) throws Exception {
		return peerMirrorState(1, depth, branch, cell);
	}

	private static WebMultiplayer.PeerMirrorState peerMirrorState(int sequence, int depth, int branch, int cell) throws Exception {
		Constructor<WebMultiplayer.PeerMirrorState> constructor =
				WebMultiplayer.PeerMirrorState.class.getDeclaredConstructor(String.class);
		constructor.setAccessible(true);
		WebMultiplayer.PeerMirrorState state = constructor.newInstance("2");
		Method update = WebMultiplayer.PeerMirrorState.class.getDeclaredMethod("update",
				int.class, int.class, int.class, int.class, String.class, String.class);
		update.setAccessible(true);
		update.invoke(state, sequence, depth, branch, cell, "move", "");
		return state;
	}

	@SuppressWarnings("unchecked")
	private static HashMap<String, WebMultiplayer.PeerMirrorState> peerMirrorMap() throws Exception {
		Field field = WebMultiplayer.class.getDeclaredField("peerMirrors");
		field.setAccessible(true);
		return (HashMap<String, WebMultiplayer.PeerMirrorState>) field.get(null);
	}

	private static void setSceneField(GameScene scene, String name, Object value) throws Exception {
		Field field = GameScene.class.getDeclaredField(name);
		field.setAccessible(true);
		field.set(scene, value);
	}

	private static class TestGameScene extends GameScene {
		final ArrayList<TestPeerMirrorSprite> created = new ArrayList<>();

		@Override
		protected PeerMirrorSprite createPeerMirrorSprite() {
			TestPeerMirrorSprite sprite = new TestPeerMirrorSprite();
			created.add(sprite);
			return sprite;
		}
	}

	private static class TestPeerMirrorSprite extends PeerMirrorSprite {
		int lastCell = -1;
		boolean lastShouldShow;
		boolean killed;
		HeroClass heroClass;
		int armorTier = -1;
		ArrayList<WebMultiplayer.PeerBuffState> peerBuffs = new ArrayList<>();

		TestPeerMirrorSprite() {
			super(false);
			visible = false;
		}

		@Override
		public int cell() {
			return lastCell;
		}

		@Override
		public void sync(int targetCell, boolean shouldShow) {
			lastCell = targetCell;
			lastShouldShow = shouldShow;
			visible = shouldShow;
		}

		@Override
		public void appearance(HeroClass heroClass, int armorTier) {
			this.heroClass = heroClass;
			this.armorTier = armorTier;
		}

		@Override
		public void peerBuffs(ArrayList<WebMultiplayer.PeerBuffState> peerBuffs) {
			this.peerBuffs = new ArrayList<>(peerBuffs);
		}

		@Override
		public void killAndErase() {
			killed = true;
			super.killAndErase();
		}
	}

	private static class TestWatcherTargetSprite extends CharSprite {
		int moveCount;
		int moveFrom = -1;
		int moveTo = -1;
		int placedCell = -1;
		int idleCount;
		int interruptCount;

		@Override
		public void move(int from, int to) {
			moveCount++;
			moveFrom = from;
			moveTo = to;
		}

		@Override
		public void place(int cell) {
			placedCell = cell;
		}

		@Override
		public void idle() {
			idleCount++;
		}

		@Override
		public void interruptMotion() {
			interruptCount++;
		}
	}

	private static class TestWatcherMobSprite extends CharSprite {
		boolean killed;

		@Override
		public void killAndErase() {
			killed = true;
		}
	}

	private static class TestMob extends Mob {
	}

	private static class TestLevel extends Level {

		TestLevel(int width, int height) {
			this.width = width;
			this.height = height;
			this.length = width * height;
			map = new int[length];
			visited = new boolean[length];
			mapped = new boolean[length];
			discoverable = new boolean[length];
			heroFOV = new boolean[length];
			passable = new boolean[length];
			losBlocking = new boolean[length];
			flamable = new boolean[length];
			secret = new boolean[length];
			solid = new boolean[length];
			avoid = new boolean[length];
			water = new boolean[length];
			pit = new boolean[length];
			openSpace = new boolean[length];
			transitions = new ArrayList<>();
			mobs = new HashSet<>();
			heaps = new com.watabou.utils.SparseArray<>();
			blobs = new java.util.HashMap<>();
			plants = new com.watabou.utils.SparseArray<>();
			traps = new com.watabou.utils.SparseArray<>();
			customTiles = new ArrayList<>();
			customWalls = new ArrayList<>();
			for (int i = 0; i < length; i++) {
				map[i] = Terrain.EMPTY;
				passable[i] = true;
				openSpace[i] = true;
			}
		}

		@Override
		protected boolean build() {
			return true;
		}

		@Override
		protected void createMobs() {
		}

		@Override
		protected void createItems() {
		}
	}
}
