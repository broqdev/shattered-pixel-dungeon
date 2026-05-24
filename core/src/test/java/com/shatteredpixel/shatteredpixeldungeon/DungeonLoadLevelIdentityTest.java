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

package com.shatteredpixel.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DungeonLoadLevelIdentityTest {

	@Test
	public void compatibleHeroAndLevelIdentityIsAccepted() {
		TestLevel level = new TestLevel();
		Hero hero = heroAt(12);

		Dungeon.LevelSaveIdentity identity = Dungeon.resolveLevelSaveIdentity(
				1, 3, 0, "game1/depth3.dat", level, hero);

		assertFalse(identity.mismatch);
		assertEquals("heroPositionCompatible", identity.reason);
		assertEquals(12, identity.heroPos);
		assertEquals(5, identity.width);
		assertEquals(5, identity.height);
		assertTrue(identity.summary().contains("depthFile=game1/depth3.dat"));
		assertTrue(identity.summary().contains("width=5"));
		assertTrue(identity.summary().contains("height=5"));
		assertTrue(identity.summary().contains("reason=heroPositionCompatible"));
	}

	@Test
	public void heroOutsideLoadedLevelIsDetectedAsMismatchedIdentity() {
		TestLevel level = new TestLevel();
		Hero hero = heroAt(200);

		Dungeon.LevelSaveIdentity identity = Dungeon.resolveLevelSaveIdentity(
				1, 3, 0, "game1/depth3.dat", level, hero);

		assertTrue(identity.mismatch);
		assertEquals("heroOutOfBounds", identity.reason);
		assertEquals(200, identity.heroPos);
		assertEquals(25, identity.length);
		assertTrue(identity.summary().contains("mismatch=true"));
	}

	@Test
	public void transitionLoadTreatsSourceLevelHeroPositionAsPendingPlacement() {
		TestLevel level = new TestLevel();
		Hero hero = heroAt(200);

		Dungeon.LevelSaveIdentity identity = Dungeon.resolveLevelSaveIdentity(
				1, 3, 0, "game1/depth3.dat", level, hero, true);

		assertTrue(identity.mismatch);
		assertTrue(identity.transitionPlacementPending);
		assertEquals("heroOutOfBounds", identity.reason);
		assertTrue(identity.summary().contains("transitionPlacementPending=true"));
	}

	@Test
	public void transitionLoadStillWarnsForMissingHero() {
		TestLevel level = new TestLevel();

		Dungeon.LevelSaveIdentity identity = Dungeon.resolveLevelSaveIdentity(
				1, 3, 0, "game1/depth3.dat", level, null, true);

		assertTrue(identity.mismatch);
		assertFalse(identity.transitionPlacementPending);
		assertEquals("missingHero", identity.reason);
	}

	@Test
	public void heroOnInvalidLoadedLevelCellIsDetectedAsMismatchedIdentity() {
		TestLevel level = new TestLevel();
		level.passable[12] = false;
		level.avoid[12] = false;
		Hero hero = heroAt(12);

		Dungeon.LevelSaveIdentity identity = Dungeon.resolveLevelSaveIdentity(
				1, 3, 0, "game1/depth3.dat", level, hero);

		assertTrue(identity.mismatch);
		assertEquals("heroInvalidPosition", identity.reason);
		assertEquals(6, identity.entrance);
		assertEquals(18, identity.exit);
	}

	@Test
	public void missingHeroIsDetectedAsMismatchedIdentity() {
		TestLevel level = new TestLevel();

		Dungeon.LevelSaveIdentity identity = Dungeon.resolveLevelSaveIdentity(
				1, 3, 0, "game1/depth3.dat", level, null);

		assertTrue(identity.mismatch);
		assertEquals("missingHero", identity.reason);
		assertEquals(-1, identity.heroPos);
	}

	private static Hero heroAt(int pos) {
		Hero hero = new Hero();
		hero.pos = pos;
		return hero;
	}

	private static class TestLevel extends Level {

		private TestLevel() {
			setSize(5, 5);
			Arrays.fill(map, Terrain.EMPTY);
			Arrays.fill(passable, true);
			transitions = new ArrayList<>();
			addTransition(6, LevelTransition.Type.REGULAR_ENTRANCE);
			addTransition(18, LevelTransition.Type.REGULAR_EXIT);
		}

		private void addTransition(int cell, LevelTransition.Type type) {
			transitions.add(new LevelTransition(this, cell, type, 3, 0, null));
			map[cell] = type == LevelTransition.Type.REGULAR_EXIT ? Terrain.EXIT : Terrain.ENTRANCE;
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
