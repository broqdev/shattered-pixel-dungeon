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

package com.watabou.noosa;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class GameSceneSwitchReadinessTest {

	private Game originalInstance;
	private Class<? extends Scene> originalSceneClass;

	@Before
	public void setUp() {
		originalInstance = Game.instance;
		originalSceneClass = Game.sceneClass;
	}

	@After
	public void tearDown() {
		Game.instance = originalInstance;
		Game.sceneClass = originalSceneClass;
	}

	@Test
	public void requestedSceneSwitchWaitsForCurrentSceneReadiness() {
		TestGame game = new TestGame();
		BlockingScene currentScene = new BlockingScene();
		game.scene = currentScene;
		game.requestedReset = true;
		Game.sceneClass = NextScene.class;

		game.step();

		assertTrue(game.requestedReset);
		assertSame(currentScene, game.scene);
		assertFalse(game.switched);
		assertTrue(game.updated);

		game.updated = false;
		currentScene.ready = true;

		game.step();

		assertFalse(game.requestedReset);
		assertTrue(game.switched);
		assertTrue(game.scene instanceof NextScene);
		assertTrue(game.updated);
	}

	private static class TestGame extends Game {
		boolean updated;
		boolean switched;

		TestGame() {
			super(NextScene.class, null);
		}

		@Override
		protected void update() {
			updated = true;
		}

		@Override
		protected void switchScene() {
			switched = true;
			scene = requestedScene;
		}
	}

	public static class BlockingScene extends Scene {
		boolean ready;

		@Override
		public boolean readyForSceneSwitch() {
			return ready;
		}
	}

	public static class NextScene extends Scene {
	}
}
