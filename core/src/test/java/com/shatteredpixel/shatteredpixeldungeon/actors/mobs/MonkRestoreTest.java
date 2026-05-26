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

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.watabou.utils.Bundle;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MonkRestoreTest {

	@Test
	public void restoreKeepsFractionalFocusCooldown() {
		TestMonk original = new TestMonk();
		original.focusCooldown(6.67f);

		Bundle bundle = new Bundle();
		original.storeInBundle(bundle);

		TestMonk restored = new TestMonk();
		restored.restoreFromBundle(bundle);

		assertEquals(6.67f, restored.focusCooldown(), 0.001f);
	}

	private static class TestMonk extends Monk {
		private void focusCooldown(float focusCooldown) {
			this.focusCooldown = focusCooldown;
		}

		private float focusCooldown() {
			return focusCooldown;
		}
	}
}
