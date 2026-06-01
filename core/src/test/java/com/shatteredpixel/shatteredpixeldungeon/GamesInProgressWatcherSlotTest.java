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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class GamesInProgressWatcherSlotTest {

	@Test
	public void watcherSlotUsesNonPlayableGameZeroFiles() {
		assertEquals(0, GamesInProgress.WATCHER_SLOT);
		assertTrue(GamesInProgress.isWatcherSlot(GamesInProgress.WATCHER_SLOT));
		assertFalse(GamesInProgress.isWatcherSlot(1));
		assertEquals("game0", GamesInProgress.gameFolder(GamesInProgress.WATCHER_SLOT));
		assertEquals("game0/game.dat", GamesInProgress.gameFile(GamesInProgress.WATCHER_SLOT));
		assertEquals("game0/depth1.dat", GamesInProgress.depthFile(GamesInProgress.WATCHER_SLOT, 1, 0));
		assertEquals("game0/depth1-branch1.dat", GamesInProgress.depthFile(GamesInProgress.WATCHER_SLOT, 1, 1));
	}
}
