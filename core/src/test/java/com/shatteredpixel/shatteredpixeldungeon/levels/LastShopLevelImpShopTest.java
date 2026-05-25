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

package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Imp;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.ImpShopRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import com.shatteredpixel.shatteredpixeldungeon.tiles.CustomTilemap;
import com.watabou.noosa.Game;
import com.watabou.utils.Bundle;
import com.watabou.utils.SparseArray;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static org.junit.Assert.assertTrue;

public class LastShopLevelImpShopTest {

	private long originalSeed;
	private int originalDepth;
	private int originalBranch;
	private Hero originalHero;
	private Level originalLevel;
	private String originalGameVersion;

	@Before
	public void setUp() {
		originalSeed = Dungeon.seed;
		originalDepth = Dungeon.depth;
		originalBranch = Dungeon.branch;
		originalHero = Dungeon.hero;
		originalLevel = Dungeon.level;
		originalGameVersion = Game.version;
	}

	@After
	public void tearDown() {
		Dungeon.seed = originalSeed;
		Dungeon.depth = originalDepth;
		Dungeon.branch = originalBranch;
		Dungeon.hero = originalHero;
		Dungeon.level = originalLevel;
		Game.version = originalGameVersion;
		Imp.Quest.reset();
	}

	@Test
	public void completedImpQuestSpawnsShopOnFirstLastShopBuild() {
		Dungeon.seed = 123456789L;
		Dungeon.depth = 21;
		Dungeon.branch = 0;
		Dungeon.hero = new Hero();
		Game.version = "test";
		restoreCompletedImpQuest();

		LastShopLevel level = new LastShopLevel();
		prepareForBuild(level);
		level.setSize(20, 20);
		TestImpShopRoom shop = new TestImpShopRoom();
		shop.set(4, 4, 12, 12);
		level.rooms = new ArrayList<>();
		level.rooms.add(shop);
		Dungeon.level = level;

		level.spawnCompletedImpShop();

		assertTrue("completed imp quest should trigger the first-build imp shop spawn", shop.spawnCalled);
	}

	private static void restoreCompletedImpQuest() {
		Bundle quests = new Bundle();
		Bundle imp = new Bundle();
		imp.put("spawned", true);
		imp.put("alternative", true);
		imp.put("given", true);
		imp.put("completed", true);
		quests.put("demon", imp);
		Imp.Quest.restoreFromBundle(quests);
	}

	private static void prepareForBuild(Level level) {
		level.transitions = new ArrayList<LevelTransition>();
		level.mobs = new HashSet<>();
		level.heaps = new SparseArray<>();
		level.blobs = new HashMap<Class<? extends Blob>, Blob>();
		level.plants = new SparseArray<Plant>();
		level.traps = new SparseArray<Trap>();
		level.customTiles = new ArrayList<CustomTilemap>();
		level.customWalls = new ArrayList<CustomTilemap>();
	}

	private static class TestImpShopRoom extends ImpShopRoom {
		private boolean spawnCalled;

		@Override
		public void spawnShop(Level level) {
			spawnCalled = true;
		}
	}
}
