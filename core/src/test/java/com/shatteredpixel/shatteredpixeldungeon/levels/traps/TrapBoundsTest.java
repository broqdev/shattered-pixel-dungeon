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

package com.shatteredpixel.shatteredpixeldungeon.levels.traps;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TrapBoundsTest {

	@Before
	public void setUp() {
		Dungeon.level = new TestLevel();
	}

	@After
	public void tearDown() {
		Dungeon.level = null;
	}

	@Test
	public void elementalTrapsSkipNeighboursOutsideMap() {
		activateAtBorder(new BurningTrap());
		activateAtBorder(new ChillingTrap());
		activateAtBorder(new OozeTrap());
		activateAtBorder(new ShockingTrap());
	}

	private void activateAtBorder(Trap trap) {
		trap.set(0);
		trap.activate();
	}

	private static class TestLevel extends Level {

		private TestLevel() {
			setSize(3, 3);
			map[4] = Terrain.WALL;
			solid[4] = true;
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
