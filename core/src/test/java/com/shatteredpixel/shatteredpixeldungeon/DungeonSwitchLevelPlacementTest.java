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

import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class DungeonSwitchLevelPlacementTest {

	@Test
	public void savedHeroPositionIsAcceptedWhenStillValid() {
		TestLevel level = new TestLevel();

		Dungeon.LevelPlacement placement = Dungeon.resolveLevelPlacement(level, 12);

		assertEquals(12, placement.requestedPos);
		assertEquals(12, placement.finalPos);
		assertEquals("requested", placement.reason);
	}

	@Test
	public void invalidSavedHeroPositionFallsBackToEntrance() {
		TestLevel level = new TestLevel();
		level.passable[12] = false;
		level.avoid[12] = false;

		Dungeon.LevelPlacement placement = Dungeon.resolveLevelPlacement(level, 12);

		assertEquals(12, placement.requestedPos);
		assertEquals(6, placement.finalPos);
		assertEquals("fallbackInvalidHeroPos", placement.reason);
	}

	@Test
	public void outOfBoundsSavedHeroPositionFallsBackToEntrance() {
		TestLevel level = new TestLevel();

		Dungeon.LevelPlacement placement = Dungeon.resolveLevelPlacement(level, -1);

		assertEquals(-1, placement.requestedPos);
		assertEquals(6, placement.finalPos);
		assertEquals("fallbackOutOfBounds", placement.reason);
	}

	@Test
	public void exitSentinelResolvesToRegularExit() {
		TestLevel level = new TestLevel();

		Dungeon.LevelPlacement placement = Dungeon.resolveLevelPlacement(level, -2);

		assertEquals(-2, placement.requestedPos);
		assertEquals(18, placement.finalPos);
		assertEquals("regularExit", placement.reason);
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
			transitions.add(new LevelTransition(this, cell, type, 1, 0, null));
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
