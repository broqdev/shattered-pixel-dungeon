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

import com.github.xpenatan.gdx.teavm.backends.shared.config.AssetFileHandle;
import com.github.xpenatan.gdx.teavm.backends.shared.config.compiler.TeaCompiler;
import com.github.xpenatan.gdx.teavm.backends.web.config.backend.WebBackend;

import org.teavm.vm.TeaVMOptimizationLevel;

import java.io.File;

public class BuildWeb {

	public static void main(String[] args) {
		WebBackend backend = new WebBackend()
				.setHtmlTitle("Shattered Pixel Dungeon")
				.setHtmlWidth(1280)
				.setHtmlHeight(720)
				.setStartJettyAfterBuild(false);

		new TeaCompiler(backend)
				.addAssets(new AssetFileHandle("../core/src/main/assets"))
				.addAssets(new AssetFileHandle("../desktop/src/main/assets"))
				.addReflectionClass("com.shatteredpixel.shatteredpixeldungeon.**")
				.addReflectionClass("com.watabou.**")
				.setMainClass("com.shatteredpixel.shatteredpixeldungeon.web.WebLauncher")
				.setOutputName("dungeon")
				.setDebugInformationGenerated(true)
				.setOptimizationLevel(TeaVMOptimizationLevel.SIMPLE)
				.setObfuscated(false)
				.build(new File("build/dist"));
	}
}
