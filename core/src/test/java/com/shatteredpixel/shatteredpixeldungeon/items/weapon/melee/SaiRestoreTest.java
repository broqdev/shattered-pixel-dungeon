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

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.watabou.utils.Bundle;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SaiRestoreTest {

	@Test
	public void restoreKeepsFractionalComboTime() {
		Bundle bundle = new Bundle();
		bundle.put("combo_time", 3.5f);
		bundle.put("recent_hits", 2);

		Sai.ComboStrikeTracker restored = new Sai.ComboStrikeTracker();
		restored.restoreFromBundle(bundle);

		Bundle restoredBundle = new Bundle();
		restored.storeInBundle(restoredBundle);

		assertEquals(3.5f, restoredBundle.getFloat("combo_time"), 0.001f);
		assertEquals(2, restoredBundle.getInt("recent_hits"));
	}
}
