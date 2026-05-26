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

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import com.watabou.utils.Bundle;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SheepRestoreTest {

	@Test
	public void restoreKeepsFractionalLifespan() {
		Sheep original = new Sheep();
		original.initialize(20.75f);

		Bundle bundle = new Bundle();
		original.storeInBundle(bundle);

		Sheep restored = new Sheep();
		restored.restoreFromBundle(bundle);

		Bundle restoredBundle = new Bundle();
		restored.storeInBundle(restoredBundle);

		assertEquals(20.75f, restoredBundle.getFloat("lifespan"), 0.001f);
	}
}
