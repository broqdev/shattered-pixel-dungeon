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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class BuildWeb {

	private static final String WEB_PARITY_LOGGING_PLACEHOLDER = "%WEB_PARITY_LOGGING%";

	public static void main(String[] args) {
		boolean release = false;
		File outputDir = new File("build/dist");
		for (int i = 0; i < args.length; i++) {
			if ("--release".equals(args[i])) {
				release = true;
			} else if ("--output".equals(args[i]) && i + 1 < args.length) {
				outputDir = new File(args[++i]);
			}
		}

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
				.setDebugInformationGenerated(!release)
				.setSourceMapsFileGenerated(!release)
				.setOptimizationLevel(TeaVMOptimizationLevel.SIMPLE)
				.setObfuscated(false)
				.build(outputDir);

		configureWebParityLogging(outputDir, !release);
	}

	private static void configureWebParityLogging(File outputDir, boolean enabled) {
		Path index = new File(new File(outputDir, "webapp"), "index.html").toPath();
		try {
			String html = Files.readString(index, StandardCharsets.UTF_8);
			if (!html.contains(WEB_PARITY_LOGGING_PLACEHOLDER)) {
				throw new IllegalStateException("Missing " + WEB_PARITY_LOGGING_PLACEHOLDER + " in " + index);
			}
			Files.writeString(index,
					html.replace(WEB_PARITY_LOGGING_PLACEHOLDER, Boolean.toString(enabled)),
					StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new RuntimeException("Unable to configure web parity logging in " + index, e);
		}
	}
}
