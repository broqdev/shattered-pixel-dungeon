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

package com.shatteredpixel.shatteredpixeldungeon.plants;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.watabou.utils.SparseArray;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PlantBoundsTest {

	@Before
	public void setUp() {
		Dungeon.level = new TestLevel();
		Dungeon.hero = new Hero();
		Dungeon.hero.subClass = HeroSubClass.WARDEN;
	}

	@After
	public void tearDown() {
		Dungeon.hero = null;
		Dungeon.level = null;
	}

	@Test
	public void wardenPlantingSkipsNeighboursOutsideMap() {
		TestSeed seed = new TestSeed();

		seed.throwAt(0);
	}

	@Test
	public void icecapActivationSkipsNeighboursOutsideMap() {
		Icecap icecap = new Icecap();
		icecap.pos = 0;

		icecap.activate(null);
	}

	public static class TestPlant extends Plant {
		@Override
		public void activate(Char ch) {
		}
	}

	private static class TestSeed extends Plant.Seed {
		private TestSeed() {
			plantClass = TestPlant.class;
		}

		private void throwAt(int cell) {
			onThrow(cell);
		}
	}

	private static class TestLevel extends Level {

		private TestLevel() {
			setSize(3, 3);
			plants = new SparseArray<>();
			traps = new SparseArray<>();
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
