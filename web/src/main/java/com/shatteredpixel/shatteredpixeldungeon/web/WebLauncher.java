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

package com.shatteredpixel.shatteredpixeldungeon.web;

import com.badlogic.gdx.Files;
import com.github.xpenatan.gdx.teavm.backends.web.WebApplication;
import com.github.xpenatan.gdx.teavm.backends.web.WebApplicationConfiguration;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.watabou.noosa.Game;
import com.watabou.utils.FileUtils;

import java.util.logging.Logger;

public class WebLauncher {

	private static final Logger log = Logger.getLogger(WebLauncher.class.getName());

	public static void main(String[] args) {
		configureLogging();

		Game.version = "3.3.8-WEB";
		Game.versionCode = 896;

		FileUtils.setDefaultFileProperties(Files.FileType.Local, "shattered-pixel-dungeon/");

		WebApplicationConfiguration config = new WebApplicationConfiguration("canvas");
		config.width = SPDSettings.DEFAULT_WINDOW_WIDTH;
		config.height = SPDSettings.DEFAULT_WINDOW_HEIGHT;
		config.showDownloadLogs = true;
		config.useGL30 = true;
		config.preloadListener = assetLoader -> assetLoader.loadScript("freetype.js");

		log.info("Starting Shattered Pixel Dungeon TeaVM web launcher");
		new WebApplication(new ShatteredPixelDungeon(new WebPlatformSupport()), config);
	}

	private static void configureLogging() {
		Logger.getLogger(WebLauncher.class.getPackage().getName())
				.info("Java logging available for TeaVM launcher");
	}
}
