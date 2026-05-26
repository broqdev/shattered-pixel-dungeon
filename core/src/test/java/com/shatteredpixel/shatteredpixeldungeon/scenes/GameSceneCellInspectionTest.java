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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;

import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GameSceneCellInspectionTest {

	@After
	public void tearDown() {
		Dungeon.level = null;
	}

	@Test
	public void rejectsCellsOutsideLevelBounds() {
		Dungeon.level = new TestLevel();

		assertFalse(GameScene.isLevelCell(null));
		assertFalse(GameScene.isLevelCell(-1));
		assertFalse(GameScene.isLevelCell(Dungeon.level.length()));
	}

	@Test
	public void acceptsOnlyVisitedOrMappedCells() {
		Dungeon.level = new TestLevel();
		Dungeon.level.visited[4] = true;
		Dungeon.level.mapped[5] = true;

		assertTrue(GameScene.isInspectableCell(4));
		assertTrue(GameScene.isInspectableCell(5));
		assertFalse(GameScene.isInspectableCell(6));
	}

	@Test
	public void acceptsCellsInsideLevelBounds() {
		Dungeon.level = new TestLevel();

		assertTrue(GameScene.isLevelCell(0));
		assertTrue(GameScene.isLevelCell(Dungeon.level.length() - 1));
	}

	private static class TestLevel extends Level {

		private TestLevel() {
			setSize(3, 3);
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
