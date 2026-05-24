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

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.github.xpenatan.gdx.teavm.backends.web.WebPreloadApplicationListener;

class SafeWebPreloadApplicationListener extends WebPreloadApplicationListener {

	private boolean stageCreated;
	private boolean logoTextureCreated;

	@Override
	protected Stage createStage() {
		Stage stage = super.createStage();
		stageCreated = true;
		return stage;
	}

	@Override
	protected Texture createTexture(FileHandle internal) {
		Texture texture = super.createTexture(internal);
		logoTextureCreated = true;
		return texture;
	}

	@Override
	public void dispose() {
		if (stageCreated && logoTextureCreated) {
			super.dispose();
		}
	}
}
