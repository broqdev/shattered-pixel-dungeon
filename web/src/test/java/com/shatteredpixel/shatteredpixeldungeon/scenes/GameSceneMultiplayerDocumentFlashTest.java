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

import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.journal.Document;
import com.watabou.noosa.Game;
import com.watabou.utils.PlatformSupport;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GameSceneMultiplayerDocumentFlashTest {

	private PlatformSupport previousPlatform;
	private GameScene previousScene;
	private Hero previousHero;
	private String previousVersion;

	@Before
	public void setUp() {
		previousPlatform = Game.platform;
		previousScene = GameScene.scene;
		previousHero = Dungeon.hero;
		previousVersion = Game.version;
		Game.platform = new MultiplayerPlatform();
		Game.version = "test";
		GameScene.scene = new GameScene();
		Dungeon.hero = null;
	}

	@After
	public void tearDown() {
		Game.platform = previousPlatform;
		Game.version = previousVersion;
		GameScene.scene = previousScene;
		Dungeon.hero = previousHero;
	}

	@Test
	public void multiplayerReadGuidePagesDoNotFlashOrOpen() {
		GameScene.flashForDocument(Document.ADVENTURERS_GUIDE, Document.GUIDE_SURPRISE_ATKS);
	}

	private static class MultiplayerPlatform extends PlatformSupport {
		@Override
		public void updateDisplaySize() {
		}

		@Override
		public void updateSystemUI() {
		}

		@Override
		public boolean connectedToUnmeteredNetwork() {
			return true;
		}

		@Override
		public boolean supportsVibration() {
			return false;
		}

		@Override
		public void setupFontGenerators(int pageSize, boolean systemFont) {
		}

		@Override
		protected FreeTypeFontGenerator getGeneratorForString(String input) {
			return null;
		}

		@Override
		public String[] splitforTextBlock(String text, boolean multiline) {
			return new String[]{text};
		}

		@Override
		public boolean multiplayerEnabled() {
			return true;
		}
	}
}
