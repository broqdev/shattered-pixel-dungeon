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

import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class InterlevelSceneDestinationCellTest {

	@Test
	public void exactTransitionTypeWins() {
		TestLevel level = new TestLevel();
		level.addTransition(6, LevelTransition.Type.REGULAR_ENTRANCE);
		level.addTransition(18, LevelTransition.Type.REGULAR_EXIT);
		level.map[22] = Terrain.EXIT;

		assertEquals(18, InterlevelScene.destinationCell(level, sourceTo(LevelTransition.Type.REGULAR_EXIT)));
	}

	@Test
	public void matchingTerrainWinsOverFallbackEntrance() {
		TestLevel level = new TestLevel();
		level.addTransition(6, LevelTransition.Type.REGULAR_ENTRANCE);
		level.map[22] = Terrain.EXIT;

		assertEquals(22, InterlevelScene.destinationCell(level, sourceTo(LevelTransition.Type.REGULAR_EXIT)));
	}

	@Test
	public void missingDestinationTypeUsesDefaultForSourceTransition() {
		TestLevel level = new TestLevel();
		level.addTransition(6, LevelTransition.Type.REGULAR_ENTRANCE);
		level.addTransition(18, LevelTransition.Type.REGULAR_EXIT);

		assertEquals(18, InterlevelScene.destinationCell(level, sourceFrom(LevelTransition.Type.REGULAR_ENTRANCE)));
	}

	@Test
	public void entranceFallbackRemainsAvailableWhenDestinationIsMissing() {
		TestLevel level = new TestLevel();
		level.addTransition(6, LevelTransition.Type.REGULAR_ENTRANCE);

		assertEquals(6, InterlevelScene.destinationCell(level, sourceTo(LevelTransition.Type.REGULAR_EXIT)));
	}

	private static LevelTransition sourceTo(LevelTransition.Type destType) {
		LevelTransition transition = new LevelTransition();
		transition.destType = destType;
		return transition;
	}

	private static LevelTransition sourceFrom(LevelTransition.Type sourceType) {
		LevelTransition transition = new LevelTransition();
		transition.type = sourceType;
		return transition;
	}

	private static class TestLevel extends Level {

		private TestLevel() {
			setSize(5, 5);
			transitions = new ArrayList<>();
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
