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

package com.shatteredpixel.shatteredpixeldungeon.levels.features;

import com.watabou.utils.Bundle;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LevelTransitionRestoreTest {

	@Test
	public void restoreFillsMissingRegularDestinationTypes() {
		assertRestoresDefault(LevelTransition.Type.REGULAR_ENTRANCE, LevelTransition.Type.REGULAR_EXIT);
		assertRestoresDefault(LevelTransition.Type.REGULAR_EXIT, LevelTransition.Type.REGULAR_ENTRANCE);
	}

	@Test
	public void restoreFillsMissingBranchDestinationTypes() {
		assertRestoresDefault(LevelTransition.Type.BRANCH_ENTRANCE, LevelTransition.Type.BRANCH_EXIT);
		assertRestoresDefault(LevelTransition.Type.BRANCH_EXIT, LevelTransition.Type.BRANCH_ENTRANCE);
	}

	@Test
	public void restoreKeepsExplicitDestinationType() {
		LevelTransition transition = restoreTransition(
				LevelTransition.Type.REGULAR_ENTRANCE,
				LevelTransition.Type.BRANCH_EXIT);

		assertEquals(LevelTransition.Type.BRANCH_EXIT, transition.destType);
	}

	private static void assertRestoresDefault(LevelTransition.Type type, LevelTransition.Type expectedDestType) {
		assertEquals(expectedDestType, restoreTransition(type, null).destType);
	}

	private static LevelTransition restoreTransition(LevelTransition.Type type, LevelTransition.Type destType) {
		Bundle bundle = new Bundle();
		bundle.put("left", 0);
		bundle.put("top", 0);
		bundle.put("right", 0);
		bundle.put("bottom", 0);
		bundle.put("center", 7);
		bundle.put(LevelTransition.TYPE, type);
		bundle.put(LevelTransition.DEST_DEPTH, 2);
		bundle.put(LevelTransition.DEST_BRANCH, 0);
		if (destType != null) {
			bundle.put(LevelTransition.DEST_TYPE, destType);
		}

		LevelTransition transition = new LevelTransition();
		transition.restoreFromBundle(bundle);
		return transition;
	}
}
