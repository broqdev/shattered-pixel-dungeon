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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.github.xpenatan.gdx.teavm.backends.web.WebApplication;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.watabou.noosa.Game;
import com.watabou.utils.Point;
import com.watabou.utils.PlatformSupport;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

import java.util.HashMap;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class WebPlatformSupport extends PlatformSupport {

	private static final Logger LOG = Logger.getLogger(WebPlatformSupport.class.getName());

	private static FreeTypeFontGenerator basicFontGenerator;
	private static FreeTypeFontGenerator asianFontGenerator;

	private final Pattern regularSplitter = Pattern.compile(
			"(?<=\n)|(?=\n)|(?<=_)|(?=_)|(?<=\\*\\*)|(?=\\*\\*)");

	private final Pattern regularSplitterMultiline = Pattern.compile(
			"(?<= )|(?= )|(?<=\n)|(?=\n)|(?<=_)|(?=_)|(?<=\\*\\*)|(?=\\*\\*)");

	private boolean defaultUiApplied;
	private final BrowserDataBackup browserDataBackup = new WebBrowserDataBackup();

	@Override
	public void updateDisplaySize() {
		Point observed = new Point(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Point stored = SPDSettings.windowResolution();
		if (observed.x > 0 && observed.y > 0 && (stored.x != observed.x || stored.y != observed.y)) {
			if (webParityLoggingEnabled()) {
				LOG.info("[WEB-PARITY] web display size observed width=" + observed.x
						+ " height=" + observed.y
						+ " storedWidth=" + stored.x
						+ " storedHeight=" + stored.y
						+ " action=storeObservedSize");
			}
			SPDSettings.windowResolution(observed);
		}
	}

	@Override
	public boolean webParityLoggingEnabled() {
		return webParityLoggingEnabledNative();
	}

	@JSBody(script = "return typeof window !== 'undefined' && window.__shpdWebParityLogging === true;")
	private static native boolean webParityLoggingEnabledNative();

	@Override
	public BrowserDataBackup browserDataBackup() {
		return browserDataBackup;
	}

	private static class WebBrowserDataBackup implements BrowserDataBackup {

		@Override
		public boolean isAvailable() {
			return browserDataBackupAvailableNative();
		}

		@Override
		public void exportData(BrowserDataBackupCallback callback) {
			if (!isAvailable()) {
				complete(callback, BrowserDataBackupResult.unavailable());
				return;
			}
			exportDataNative(Game.version, (success, message, localStorageKeys, indexedDbRecords, bytes) ->
					complete(callback, result(success, message, localStorageKeys, indexedDbRecords, bytes)));
		}

		@Override
		public void importData(BrowserDataBackupCallback callback) {
			if (!isAvailable()) {
				complete(callback, BrowserDataBackupResult.unavailable());
				return;
			}
			importDataNative((success, message, localStorageKeys, indexedDbRecords, bytes) ->
					complete(callback, result(success, message, localStorageKeys, indexedDbRecords, bytes)));
		}

		private void complete(BrowserDataBackupCallback callback, BrowserDataBackupResult result) {
			if (callback != null) {
				callback.onComplete(result);
			}
		}

		private BrowserDataBackupResult result(boolean success, String message, int localStorageKeys,
				int indexedDbRecords, double bytes) {
			long byteCount = Double.isNaN(bytes) || bytes < 0 ? 0 : (long)bytes;
			if (success) {
				return BrowserDataBackupResult.success(message, localStorageKeys, indexedDbRecords, byteCount);
			} else {
				return BrowserDataBackupResult.failure(message);
			}
		}

		@JSFunctor
		private interface BrowserDataBackupCompletion extends JSObject {
			void complete(boolean success, String message, int localStorageKeys, int indexedDbRecords, double bytes);
		}

		@JSBody(script = "return typeof window !== 'undefined'"
				+ " && !!window.__shpdBrowserDataBackup"
				+ " && typeof window.__shpdBrowserDataBackup.available === 'function'"
				+ " && window.__shpdBrowserDataBackup.available();")
		private static native boolean browserDataBackupAvailableNative();

		@JSBody(params = { "gameVersion", "completion" },
				script = "window.__shpdBrowserDataBackup.exportData(gameVersion, completion);")
		private static native void exportDataNative(String gameVersion, BrowserDataBackupCompletion completion);

		@JSBody(params = { "completion" },
				script = "window.__shpdBrowserDataBackup.importData(completion);")
		private static native void importDataNative(BrowserDataBackupCompletion completion);
	}

	@Override
	public boolean supportsFullScreen() {
		return false;
	}

	@Override
	public void updateSystemUI() {
		if (!defaultUiApplied) {
			defaultUiApplied = true;
			if (!WebApplication.isMobileDevice() && !SPDSettings.contains(SPDSettings.KEY_UI_SIZE)) {
				SPDSettings.interfaceSize(2);
			}
		}
		// Browser chrome and safe-area behavior are owned by the page.
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
		if (fonts != null && this.pageSize == pageSize && this.systemfont == systemFont) {
			return;
		}
		this.pageSize = pageSize;
		this.systemfont = systemFont;

		resetGenerators(false);
		fonts = new HashMap<>();

		if (systemFont) {
			basicFontGenerator = asianFontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/droid_sans.ttf"));
		} else {
			basicFontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/pixel_font.ttf"));
			asianFontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/droid_sans.ttf"));
		}

		fonts.put(basicFontGenerator, new HashMap<Integer, BitmapFont>());
		fonts.put(asianFontGenerator, new HashMap<Integer, BitmapFont>());

		packer = new PixmapPacker(pageSize, pageSize, Pixmap.Format.RGBA8888, 1, false);
	}

	@Override
	protected FreeTypeFontGenerator getGeneratorForString(String input) {
		if (containsAsianScript(input)) {
			return asianFontGenerator;
		} else {
			return basicFontGenerator;
		}
	}

	private static boolean containsAsianScript(String input) {
		if (input == null) {
			return false;
		}
		for (int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);
			if ((c >= '\uAC00' && c <= '\uD7AF')
					|| (c >= '\u4E00' && c <= '\u9FFF')
					|| (c >= '\u3000' && c <= '\u303F')
					|| (c >= '\uFF00' && c <= '\uFFEF')
					|| (c >= '\u3040' && c <= '\u309F')
					|| (c >= '\u30A0' && c <= '\u30FF')) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String[] splitforTextBlock(String text, boolean multiline) {
		if (multiline) {
			return regularSplitterMultiline.split(text);
		} else {
			return regularSplitter.split(text);
		}
	}
}
