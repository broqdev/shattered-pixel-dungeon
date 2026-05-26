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

package com.shatteredpixel.shatteredpixeldungeon.tiles;

import com.shatteredpixel.shatteredpixeldungeon.levels.Level;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WallBlockingTilemapBoundsTest {

	@Test
	public void clearsBorderCellsBeforeNeighbourInspection() {
		TestLevel level = new TestLevel();

		assertTrue(WallBlockingTilemap.clearsCell(level, 1, level.width(), level.length()));
		assertTrue(WallBlockingTilemap.clearsCell(level, 7, level.width(), level.length()));
		assertFalse(WallBlockingTilemap.clearsCell(level, 4, level.width(), level.length()));
	}

	@Test
	public void clearsInvalidOrUndiscoverableCells() {
		TestLevel level = new TestLevel();
		level.discoverable[4] = false;

		assertTrue(WallBlockingTilemap.clearsCell(null, 4, level.width(), level.length()));
		assertTrue(WallBlockingTilemap.clearsCell(level, -1, level.width(), level.length()));
		assertTrue(WallBlockingTilemap.clearsCell(level, level.length(), level.width(), level.length()));
		assertTrue(WallBlockingTilemap.clearsCell(level, 4, level.width(), level.length()));
	}

	private static class TestLevel extends Level {

		private TestLevel() {
			setSize(3, 3);
			discoverable = new boolean[length()];
			Arrays.fill(discoverable, true);
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
